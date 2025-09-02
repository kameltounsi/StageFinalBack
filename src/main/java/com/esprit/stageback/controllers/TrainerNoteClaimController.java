// src/main/java/com/esprit/stageback/controllers/TrainerNoteClaimController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.ClaimDecisionRequest;
import com.esprit.stageback.services.TrainerNoteClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer/notes/claims")
@RequiredArgsConstructor
public class TrainerNoteClaimController {

    private final TrainerNoteClaimService service;

    /** GET inbox (pending & visible for this trainer) */
    @GetMapping
    public ResponseEntity<List<NoteClaimDTO>> inbox(Authentication auth) {
        return ResponseEntity.ok(service.inbox(auth.getName()));
    }

    /** PATCH approve */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<NoteClaimDTO> approve(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody(required = false) ClaimDecisionRequest req
    ) {
        return ResponseEntity.ok(service.approve(auth.getName(), id, req));
    }

    /** PATCH reject */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<NoteClaimDTO> reject(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody ClaimDecisionRequest req
    ) {
        return ResponseEntity.ok(service.reject(auth.getName(), id, req));
    }
}
