// src/main/java/com/esprit/stageback/controllers/StudentDashboardController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.DaySessionDTO;
import com.esprit.stageback.dto.GradeRowDTO;
import com.esprit.stageback.dto.StudentOverviewDTO;
import com.esprit.stageback.services.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/student/dashboard")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService service;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<StudentOverviewDTO> overview(Principal principal) {
        return ResponseEntity.ok(service.overview(principal));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<List<DaySessionDTO>> today(Principal principal) {
        return ResponseEntity.ok(service.today(principal));
    }

    @GetMapping("/grades")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<List<GradeRowDTO>> grades(Principal principal) {
        return ResponseEntity.ok(service.grades(principal));
    }
}
