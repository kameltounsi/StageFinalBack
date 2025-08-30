// src/main/java/com/esprit/stageback/controllers/StudentAbsenceController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.StudentAbsenceSummaryDTO;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.StudentAbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/students/me/absences")
@RequiredArgsConstructor
public class StudentAbsenceController {

    private final StudentAbsenceService service;
    private final UserRepository userRepo;

    private Long me(Principal p) {
        return userRepo.findByEmail(p.getName()).orElseThrow().getId();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','TRAINER')") // autorise l’élève (et admin/coach si besoin)
    public ResponseEntity<StudentAbsenceSummaryDTO> myAbsences(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Principal principal
    ) {
        Long studentId = me(principal);
        LocalDate s = (start == null || start.isBlank()) ? null : LocalDate.parse(start);
        LocalDate e = (end   == null || end.isBlank())   ? null : LocalDate.parse(end);
        return ResponseEntity.ok(service.myAbsences(studentId, s, e));
    }
}
