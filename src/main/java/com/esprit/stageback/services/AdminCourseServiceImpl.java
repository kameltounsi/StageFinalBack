// src/main/java/com/esprit/stageback/services/AdminCourseServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.GroupDTO;
import com.esprit.stageback.dto.TrainerDTO;
import com.esprit.stageback.entities.CourseFile;
import com.esprit.stageback.repositories.CourseFileRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.repositories.spec.CourseFileSpecs;
import com.esprit.stageback.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseFileRepository repo;
    private final StorageService storage;
    private final UserRepository userRepo;
    private final GroupeRepository groupeRepo; // NEW

    @Value("${storage.presign.exp-min:60}")
    private int defaultExpMin;
    @Override
    public List<CourseFileDTO> list(Long groupeId, String subject, Long trainerId, String query, String specialite) {
        subject    = subject == null ? null : subject.trim();
        query      = query == null ? null : query.trim();
        specialite = specialite == null ? null : specialite.trim();

        var spec = Specification.<CourseFile>where(null);

        if (specialite != null && !specialite.isBlank()) {
            spec = spec.and(CourseFileSpecs.anySpecialiteEqIgnoreCase(specialite));
        }
        if (groupeId != null) {
            spec = spec.and(CourseFileSpecs.groupeIdEq(groupeId));
        }
        if (subject != null && !subject.isBlank()) {
            spec = spec.and(CourseFileSpecs.subjectEqIgnoreCase(subject));
        }
        if (trainerId != null) {
            // ⬇️ au lieu de trainerIdEq : attrape aussi les rows “email only”
            spec = spec.and(CourseFileSpecs.trainerMatches(trainerId));
        }
        if (query != null && !query.isBlank()) {
            spec = spec.and(CourseFileSpecs.titleContainsIgnoreCase(query));
        }

        return repo.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Meta meta() {
        List<GroupDTO> groups = repo.findDistinctGroupeNames();
        List<String> subjects = repo.findDistinctSubjects();
        List<TrainerDTO> trainers = userRepo.findAllTrainerSummaries();

        // 🔀 Union case-insensitive des spécialités trainer ∪ groupes
        var trainerSpecs = userRepo.findDistinctTrainerSpecialites();
        var groupSpecs   = groupeRepo.findDistinctSpecialites();

        List<String> specialites =
                Stream.concat(trainerSpecs.stream(), groupSpecs.stream())
                        .filter(s -> s != null && !s.isBlank())
                        .map(s -> s.trim().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new))   // unique + ordre d’insertion
                        .stream().toList();

        return new Meta(groups, subjects, trainers, specialites);
    }

    @Override
    public List<String> subjects(Long groupeId) {
        return repo.findDistinctSubjectsForGroupOrAll(groupeId);
    }

    @Override
    public String presignedUrl(Long id, int minutes) {
        var cf = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        int exp = minutes > 0 ? minutes : defaultExpMin;
        if (exp > 24 * 60) exp = 24 * 60;
        URL url = storage.presignGet(cf.getS3Key(), Duration.ofMinutes(exp));
        return url.toString();
    }

    @Override
    public void delete(Long id) {
        var cf = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        try { storage.delete(cf.getS3Key()); } catch (Exception ignored) {}
        repo.deleteById(id);
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
                .trainerId(c.getTrainerId())
                .trainerEmail(c.getTrainerEmail())
                .build();
    }
}
