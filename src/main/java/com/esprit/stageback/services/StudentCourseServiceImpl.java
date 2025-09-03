// src/main/java/com/esprit/stageback/services/StudentCourseServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.SubjectTeacherDTO;
import com.esprit.stageback.entities.CourseFile;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.CourseFileRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final UserRepository userRepo;
    private final CourseFileRepository repo;
    private final StorageService storage;

    @Value("${storage.presign.exp-min:60}")
    private int defaultExpMin;

    private Long requireStudentGroup(String email) {
        // ⚠️ Utiliser la méthode existante du repo
        return userRepo.findGroupIdByEmailIgnoreCase(email)
                .orElseThrow(() -> new SecurityException("No group assigned to this student."));
    }

    @Override
    public List<String> mySubjects(String studentEmail) {
        Long gid = requireStudentGroup(studentEmail);
        return repo.findDistinctSubjectsForGroup(gid);
    }

    @Override
    public List<CourseFileDTO> myCourses(String studentEmail, String subject) {
        Long gid = requireStudentGroup(studentEmail);
        List<CourseFile> list = (subject == null || subject.isBlank())
                ? repo.findByGroupeIdOrderByCreatedAtDesc(gid)
                : repo.findByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(gid, subject);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public String presignedUrl(String studentEmail, Long courseId, int minutes) {
        Long gid = requireStudentGroup(studentEmail);
        CourseFile cf = repo.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (!gid.equals(cf.getGroupeId())) throw new SecurityException("Forbidden.");
        int exp = minutes > 0 ? minutes : defaultExpMin;
        if (exp > 24 * 60) exp = 24 * 60;
        URL url = storage.presignGet(cf.getS3Key(), Duration.ofMinutes(exp));
        return url.toString();
    }

    @Override
    public Map<String, SubjectTeacherDTO> subjectsMeta(String studentEmail) {
        Long gid = requireStudentGroup(studentEmail);

        // Toutes les matières connues du groupe
        List<String> subjects = repo.findDistinctSubjectsForGroup(gid);
        Map<String, SubjectTeacherDTO> out = new LinkedHashMap<>();

        // Fallback prof = premier trainer du groupe
        List<User> trainers = userRepo.findTrainersByGroupeId(gid);
        User fallback = (trainers != null && !trainers.isEmpty()) ? trainers.get(0) : null;

        for (String s : subjects) {
            SubjectTeacherDTO dto = new SubjectTeacherDTO();

            // Dernier fichier envoyé pour cette matière => on récupère trainerId
            CourseFile latest = repo.findTopByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(gid, s);
            if (latest != null && latest.getTrainerId() != null) {
                userRepo.findById(latest.getTrainerId()).ifPresentOrElse(t -> {
                    dto.setTeacherName(safeName(t.getFullName()));
                    dto.setTeacherAvatarUrl(safeUrl(t.getProfilePicture()));
                }, () -> applyFallback(dto, fallback));
            } else {
                applyFallback(dto, fallback);
            }
            out.put(s, dto);
        }
        return out;
    }

    private void applyFallback(SubjectTeacherDTO dto, User f) {
        if (f != null) {
            dto.setTeacherName(safeName(f.getFullName()));
            dto.setTeacherAvatarUrl(safeUrl(f.getProfilePicture()));
        } else {
            dto.setTeacherName("Teacher");
            dto.setTeacherAvatarUrl(null);
        }
    }

    private String safeName(String s) {
        return (s == null || s.isBlank()) ? "Teacher" : s.trim();
    }

    private String safeUrl(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private CourseFileDTO toDto(CourseFile c) {
        return CourseFileDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .contentType(c.getContentType())
                .sizeBytes(c.getSizeBytes())
                .createdAt(c.getCreatedAt())
                .groupeId(c.getGroupeId())
                .groupeName(c.getGroupeName())
                .subject(c.getSubject())
                .build();
    }
}
