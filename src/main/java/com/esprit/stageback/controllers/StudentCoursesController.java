// src/main/java/com/esprit/stageback/controllers/StudentCoursesController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.SubjectTeacherDTO;
import com.esprit.stageback.services.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCoursesController {

    private final StudentCourseService service;

    // Liste des matières (rooms) du groupe de l’étudiant
    @GetMapping("/subjects")
    public ResponseEntity<List<String>> subjects(Authentication auth) {
        return ResponseEntity.ok(service.mySubjects(auth.getName()));
    }

    // NEW: méta par matière (nom + avatar prof)
    @GetMapping("/subjects/meta")
    public ResponseEntity<Map<String, SubjectTeacherDTO>> subjectsMeta(Authentication auth) {
        return ResponseEntity.ok(service.subjectsMeta(auth.getName()));
    }

    // Liste des cours (tout ou filtré par matière)
    @GetMapping
    public ResponseEntity<List<CourseFileDTO>> list(Authentication auth,
                                                    @RequestParam(value = "subject", required = false) String subject) {
        return ResponseEntity.ok(service.myCourses(auth.getName(), subject));
    }

    // Lien de téléchargement pré-signé
    @GetMapping("/{id}/download")
    public ResponseEntity<String> download(Authentication auth,
                                           @PathVariable Long id,
                                           @RequestParam(value = "expMin", defaultValue = "60") int expMin) {
        return ResponseEntity.ok(service.presignedUrl(auth.getName(), id, expMin));
    }
}
