// src/main/java/com/esprit/stageback/controllers/TrainerDashboardController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.services.TrainerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers/me")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200","http://127.0.0.1:4200"}, allowCredentials = "true")
public class TrainerDashboardController {

    private final TrainerDashboardService service;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<TrainerOverviewDTO> overview() {
        return ResponseEntity.ok(service.overview());
    }

    @GetMapping("/groups")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<TrainerGroupRowDTO>> myGroups() {
        return ResponseEntity.ok(service.myGroups());
    }

    @GetMapping("/groups/{id}/students")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<StudentMiniDTO>> students(@PathVariable Long id) {
        return ResponseEntity.ok(service.studentsOfGroup(id));
    }

    @GetMapping("/schedule/week")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<ScheduleItemDTO>> week(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start
    ){
        return ResponseEntity.ok(service.weeklySchedule(start));
    }
}
