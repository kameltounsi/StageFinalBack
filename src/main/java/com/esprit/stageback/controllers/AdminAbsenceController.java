// src/main/java/com/esprit/stageback/controllers/AdminAbsenceController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.dto.AdminAbsenceSummaryRow;
import com.esprit.stageback.dto.SendAlertRequest;
import com.esprit.stageback.dto.SendBulkAlertRequest;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.services.AdminAbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    // -------- NEW: single alert --------
    @PostMapping("/alerts/send")
    public ResponseEntity<?> sendAlert(@RequestBody SendAlertRequest req) {
        if (req.getStudentId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "studentId is required"));
        }
        long threshold = req.getMinUnjustified() == null ? 5L : req.getMinUnjustified();
        service.sendAlertToStudent(req.getStudentId(), threshold);
        return ResponseEntity.ok().build();
    }

    // -------- NEW: bulk alerts --------
    @PostMapping("/alerts/send-bulk")
    public ResponseEntity<?> sendBulk(@RequestBody SendBulkAlertRequest req) {
        LocalDate s = (req.getStart() == null || req.getStart().isBlank()) ? null : LocalDate.parse(req.getStart());
        LocalDate e = (req.getEnd()   == null || req.getEnd().isBlank())   ? null : LocalDate.parse(req.getEnd());
        long threshold = req.getMinUnjustified() == null ? 5L : req.getMinUnjustified();
        int sent = service.sendBulkAlerts(req.getSpecialite(), req.getGroupId(), s, e, threshold);
        return ResponseEntity.ok(Map.of("sent", sent));
    }
}
