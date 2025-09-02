// src/main/java/com/esprit/stageback/controllers/StudentNotesController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.StudentNoteDTO;
import com.esprit.stageback.services.StudentNotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/notes")
@RequiredArgsConstructor
public class StudentNotesController {

    private final StudentNotesService service;

    // GET /api/student/notes
    @GetMapping
    public ResponseEntity<List<StudentNoteDTO>> myNotes(Authentication auth) {
        List<StudentNoteDTO> list = service.myNotes(auth.getName());
        return ResponseEntity.ok(list);
    }
}
