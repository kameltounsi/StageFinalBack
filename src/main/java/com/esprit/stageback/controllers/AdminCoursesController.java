// src/main/java/com/esprit/stageback/controllers/AdminCoursesController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.services.AdminCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCoursesController {

    private final AdminCourseService service;

    @GetMapping(value = "/meta", produces = "application/json")
    public ResponseEntity<AdminCourseService.Meta> meta() {
        return ResponseEntity.ok(service.meta());
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<CourseFileDTO>> list(
            @RequestParam(required = false, name = "groupId") Long groupId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String specialite   // ← NEW: filtre par spécialité
    ) {
        return ResponseEntity.ok(service.list(groupId, subject, trainerId, query, specialite));
    }

    @GetMapping(value = "/{id}/download", produces = "application/json")
    public ResponseEntity<String> download(@PathVariable Long id,
                                           @RequestParam(defaultValue = "60", name = "expMin") int expMin) {
        return ResponseEntity.ok(service.presignedUrl(id, expMin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
