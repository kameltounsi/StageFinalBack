package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AdminOverviewDTO;
import com.esprit.stageback.dto.GroupStatusRowDTO;
import com.esprit.stageback.dto.SpecialiteCountDTO;
import com.esprit.stageback.dto.TrainersBySpecialiteDTO;
import com.esprit.stageback.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOverviewDTO> overview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/groups/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GroupStatusRowDTO>> groupStatus() {
        return ResponseEntity.ok(dashboardService.getGroupStatus());
    }

    // NEW: Trainers par spécialité (+ groupes)
    @GetMapping("/trainers/by-specialite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrainersBySpecialiteDTO>> trainersBySpecialite() {
        return ResponseEntity.ok(dashboardService.getTrainersBySpecialite());
    }
    @GetMapping("/stats/students-by-specialite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SpecialiteCountDTO>> studentsBySpecialite() {
        return ResponseEntity.ok(dashboardService.getStudentsBySpecialite());
    }
}
