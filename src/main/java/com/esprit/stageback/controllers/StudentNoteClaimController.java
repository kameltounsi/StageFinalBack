// src/main/java/com/esprit/stageback/controllers/StudentNoteClaimController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.NoteClaimRequest;
import com.esprit.stageback.services.StudentNoteClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/notes/claims")
@RequiredArgsConstructor
public class StudentNoteClaimController {

    private final StudentNoteClaimService service;

    @PostMapping
    public ResponseEntity<NoteClaimDTO> submit(Authentication auth, @RequestBody NoteClaimRequest req) {
        return ResponseEntity.ok(service.submitClaim(auth.getName(), req));
    }

    @GetMapping
    public ResponseEntity<List<NoteClaimDTO>> myClaims(Authentication auth) {
        return ResponseEntity.ok(service.myClaims(auth.getName()));
    }
}
