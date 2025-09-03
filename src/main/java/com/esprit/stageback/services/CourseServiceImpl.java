// src/main/java/com/esprit/stageback/services/CourseServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.entities.CourseFile;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.CourseFileRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service: uploads trainer course PDFs to object storage (Wasabi S3 via StorageService abstraction)
 * Hardening:
 *  - try-with-resources when reading MultipartFile stream
 *  - clear, actionable error messages surfaced from underlying S3 exceptions (requestId/errorCode when available)
 *  - normalized S3 keys (no leading slash)
 *  - strict content-type & empty-file guards
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseFileRepository repo;
    private final GroupeRepository groupeRepo;
    private final UserRepository userRepo;
    private final StorageService storage;

    @Value("${storage.presign.exp-min:60}")
    private int defaultExpMin;

    private void assertTrainerOwnsGroup(String trainerEmail, Long groupeId) {
        List<Long> gids = userRepo.findTrainerGroupIdsByEmailIgnoreCase(trainerEmail);
        if (gids == null || gids.stream().noneMatch(id -> Objects.equals(id, groupeId))) {
            throw new SecurityException("You are not assigned to this group.");
        }
    }

    private Groupe loadGroupOrThrow(Long groupeId) {
        return groupeRepo.findById(groupeId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    private void assertSubjectAllowed(Groupe g, String subject) {
        List<String> allowed = SubjectCatalog.subjectsFor(g.getSpecialite());
        if (allowed.stream().noneMatch(s -> s.equalsIgnoreCase(subject))) {
            throw new IllegalArgumentException("Subject not allowed for this group's speciality.");
        }
    }

    @Override
    public CourseFileDTO uploadCourse(String trainerEmail, Long trainerId, Long groupeId, String subject, MultipartFile pdf) {
        if (pdf == null || pdf.isEmpty()) throw new IllegalArgumentException("Empty file.");
        // Some browsers send application/octet-stream; enforce PDF by original filename fallback if needed
        String contentType = pdf.getContentType();
        if (contentType == null || contentType.isBlank()) contentType = "application/pdf"; // fallback
        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Only PDF is allowed.");
        }
        if (groupeId == null || subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Group and subject are required.");
        }

        // 1) sécurité: le trainer doit posséder ce groupe
        assertTrainerOwnsGroup(trainerEmail, groupeId);

        // 2) validation matière par spécialité
        Groupe g = loadGroupOrThrow(groupeId);
        assertSubjectAllowed(g, subject);

        // 3) clé S3 séquentielle, sans leading '/'
        String datePath = DateTimeFormatter.ofPattern("yyyy/MM").format(java.time.LocalDate.now());
        String key = "trainer/%d/group-%d/%s/%s.pdf".formatted(trainerId, groupeId, datePath, UUID.randomUUID());

        // 4) upload
        try (InputStream in = pdf.getInputStream()) {
            storage.upload(key, contentType, pdf.getSize(), in);
        } catch (Exception e) {
            // Try to unwrap useful info (e.g., S3 requestId/errorCode if StorageService surfaces it)
            StringBuilder sb = new StringBuilder("Upload failed");
            Throwable t = e;
            while (t != null) {
                String cn = t.getClass().getSimpleName();
                String msg = t.getMessage();
                if (msg != null && !msg.isBlank()) {
                    sb.append(" | ").append(cn).append(": ").append(msg);
                } else {
                    sb.append(" | ").append(cn);
                }
                t = t.getCause();
            }
            log.error("Wasabi upload error for key {}: {}", key, sb.toString(), e);
            throw new RuntimeException(sb.toString(), e);
        }

        CourseFile saved = repo.save(CourseFile.builder()
                .title(pdf.getOriginalFilename())
                .s3Key(key)
                .contentType(contentType)
                .sizeBytes(pdf.getSize())
                .trainerId(trainerId)
                .trainerEmail(trainerEmail)
                .groupeId(g.getId())
                .groupeName(g.getNom())
                .subject(subject)
                .createdAt(Instant.now())
                .build());

        return toDto(saved, null);
    }

    @Override
    public List<CourseFileDTO> myCourses(Long trainerId, Long groupeId) {
        List<CourseFile> list = (groupeId == null)
                ? repo.findByTrainerIdOrderByCreatedAtDesc(trainerId)
                : repo.findByTrainerIdAndGroupeIdOrderByCreatedAtDesc(trainerId, groupeId);
        return list.stream().map(cf -> toDto(cf, null)).toList();
    }

    @Override
    public void deleteCourse(String trainerEmail, Long trainerId, Long id) {
        CourseFile cf = repo.findById(id).orElseThrow();
        // le trainer doit posséder ce groupe
        assertTrainerOwnsGroup(trainerEmail, cf.getGroupeId());
        if (!cf.getTrainerId().equals(trainerId)) throw new SecurityException("Forbidden.");
        try {
            storage.delete(cf.getS3Key());
        } catch (Exception e) {
            log.error("Wasabi delete error for key {}: {}", cf.getS3Key(), e.getMessage(), e);
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
        repo.delete(cf);
    }

    @Override
    public String presignedUrl(Long trainerId, Long id, int minutes) {
        CourseFile cf = repo.findById(id).orElseThrow();
        if (!cf.getTrainerId().equals(trainerId)) throw new SecurityException("Forbidden.");
        int exp = minutes > 0 ? minutes : defaultExpMin;
        // optional guardrail: cap to 24h
        if (exp > (24 * 60)) exp = 24 * 60;
        URL url = storage.presignGet(cf.getS3Key(), Duration.ofMinutes(exp));
        return url.toString();
    }

    @Override
    public List<String> allowedSubjectsForGroup(Long groupeId) {
        Groupe g = loadGroupOrThrow(groupeId);
        return SubjectCatalog.subjectsFor(g.getSpecialite());
    }

    private CourseFileDTO toDto(CourseFile c, String url) {
        return CourseFileDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .contentType(c.getContentType())
                .sizeBytes(c.getSizeBytes())
                .createdAt(c.getCreatedAt())
                .groupeId(c.getGroupeId())
                .groupeName(c.getGroupeName())
                .subject(c.getSubject())
                .presignedUrl(url)
                .build();
    }
}
