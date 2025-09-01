// src/main/java/com/esprit/stageback/controllers/AdminAbsenceController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.dto.AdminAbsenceSummaryRow;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.services.AdminAbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAbsenceController {

    private final AdminAbsenceService service;

    @GetMapping("/specialites")
    public ResponseEntity<List<String>> specialites() {
        return ResponseEntity.ok(service.listSpecialites());
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Groupe>> groups(@RequestParam String specialite) {
        return ResponseEntity.ok(service.groupsBySpecialite(specialite));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<AdminAbsenceSummaryRow>> summary(
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "total") String sortBy,   // total | unjustified | name
            @RequestParam(defaultValue = "desc") String dir        // asc | desc
    ) {
        LocalDate s = (start == null || start.isBlank()) ? null : LocalDate.parse(start);
        LocalDate e = (end   == null || end.isBlank())   ? null : LocalDate.parse(end);
        return ResponseEntity.ok(service.summary(specialite, groupId, s, e, sortBy, dir));
    }

    @GetMapping("/students/{studentId}/details")
    public ResponseEntity<List<AdminAbsenceDetailItem>> details(
            @PathVariable Long studentId,
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        LocalDate s = (start == null || start.isBlank()) ? null : LocalDate.parse(start);
        LocalDate e = (end   == null || end.isBlank())   ? null : LocalDate.parse(end);
        return ResponseEntity.ok(service.detailsForStudent(studentId, specialite, groupId, s, e));
    }
}
