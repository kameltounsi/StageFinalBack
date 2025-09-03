// src/main/java/com/esprit/stageback/controllers/TrainerCoursesController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.UploadCourseResponse;
import com.esprit.stageback.services.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainer/courses")
@RequiredArgsConstructor
public class TrainerCoursesController {

    private final CourseService service;

    // Upload PDF (GROUPE + SUBJECT requis)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<UploadCourseResponse> upload(
            Authentication auth,
            @RequestParam("file") MultipartFile pdf,
            @RequestParam("trainerId") Long trainerId,
            @RequestParam("groupeId") Long groupeId,
            @RequestParam("subject") String subject
    ) {
        var dto = service.uploadCourse(auth.getName(), trainerId, groupeId, subject, pdf);
        return ResponseEntity.ok(UploadCourseResponse.builder()
                .id(dto.getId()).title(dto.getTitle())
                .groupeId(dto.getGroupeId())
                .subject(dto.getSubject())
                .build());
    }

    // Liste (optionnel: groupeId)
    @GetMapping
    public ResponseEntity<List<CourseFileDTO>> list(Authentication auth,
                                                    @RequestParam("trainerId") Long trainerId,
                                                    @RequestParam(value = "groupeId", required = false) Long groupeId) {
        return ResponseEntity.ok(service.myCourses(trainerId, groupeId));
    }

    // Lien téléchargement
    @GetMapping("/{id}/download")
    public ResponseEntity<String> download(Authentication auth,
                                           @PathVariable Long id,
                                           @RequestParam("trainerId") Long trainerId,
                                           @RequestParam(value = "expMin", defaultValue = "60") int expMin) {
        return ResponseEntity.ok(service.presignedUrl(trainerId, id, expMin));
    }

    // Suppression
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth,
                                       @PathVariable Long id,
                                       @RequestParam("trainerId") Long trainerId) {
        service.deleteCourse(auth.getName(), trainerId, id);
        return ResponseEntity.noContent().build();
    }

    // Obtenir les 3 matières autorisées pour un groupe
    @GetMapping("/subjects")
    public ResponseEntity<List<String>> subjects(@RequestParam("groupeId") Long groupeId) {
        return ResponseEntity.ok(service.allowedSubjectsForGroup(groupeId));
    }
}
