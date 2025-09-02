// src/main/java/com/esprit/stageback/controllers/TrainerNotesController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.GradeRow;
import com.esprit.stageback.dto.SaveGradesRequest;
import com.esprit.stageback.services.TrainerNotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer/notes")
@RequiredArgsConstructor
public class TrainerNotesController {

    private final TrainerNotesService service;

    @GetMapping("/sheet")
    public ResponseEntity<?> sheet(
            @RequestParam(name = "groupeId", required = false) Long groupeId,
            @RequestParam(name = "groupId",  required = false) Long groupId,
            @RequestParam String matiere,
            Authentication auth
    ) {
        Long gid = (groupeId != null) ? groupeId : groupId;
        if (gid == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Missing parameter: groupeId (or groupId)"));
        }

        try {
            // ➜ version sans date
            List<GradeRow> rows = service.loadSheet(auth.getName(), gid, matiere);
            return ResponseEntity.ok(rows);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("message", se.getMessage()));
        }
    }

    // Accepte POST sur /sheet (et /bulk pour rétro-compatibilité)
    @PostMapping({"/sheet", "/bulk"})
    public ResponseEntity<?> save(@RequestBody SaveGradesRequest payload, Authentication auth) {
        if (payload.getGroupeId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing groupeId (or groupId) in payload"));
        }
        if (payload.getMatiere() == null || payload.getMatiere().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing matiere"));
        }

        try {
            service.saveBulk(auth.getName(), payload);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("message", se.getMessage()));
        }
    }
}
