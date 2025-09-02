package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.GradeRow;
import com.esprit.stageback.dto.SaveGradesRequest;
import com.esprit.stageback.services.TrainerNotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/trainer", "/api/trainers"})
@RequiredArgsConstructor
public class TrainerNotesController {

    private final TrainerNotesService service;

    // ---- FEUILLE ----
    @GetMapping("/notes/sheet")
    public ResponseEntity<?> sheet(
            @RequestParam(name = "groupeId", required = false) Long groupeId,
            @RequestParam(name = "groupId",  required = false) Long groupId, // toléré
            @RequestParam String matiere,
            @RequestParam String date,
            Authentication auth
    ) {
        Long gid = (groupeId != null) ? groupeId : groupId;
        if (gid == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing parameter: groupeId (or groupId)"));
        }

        LocalDate d;
        try {
            d = LocalDate.parse(date); // yyyy-MM-dd
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid date format, expected yyyy-MM-dd",
                    "invalid", date
            ));
        }

        try {
            List<GradeRow> rows = service.loadSheet(auth.getName(), gid, d, matiere);
            return ResponseEntity.ok(rows);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("message", se.getMessage()));
        }
    }

    // ---- SAUVEGARDE ----
    @PostMapping("/notes/bulk")
    public ResponseEntity<?> save(@RequestBody SaveGradesRequest payload, Authentication auth) {
        if (payload.getGroupeId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing groupeId (or groupId) in payload"));
        }
        if (payload.getMatiere() == null || payload.getMatiere().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing matiere"));
        }
        if (payload.getDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing date (use yyyy-MM-dd)"));
        }

        try {
            service.saveBulk(auth.getName(), payload);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("message", se.getMessage()));
        }
    }

    // OPTION : si tu veux **aussi** supporter l’ancien chemin /api/notes/*
    // dé-commente ces deux mappings :
    /*
    @GetMapping("/../notes/sheet") // <- ne compile pas tel quel, préfère créer un 2e contrôleur @RequestMapping("/api/notes")
    @PostMapping("/../notes/bulk")
    */
}
