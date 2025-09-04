// src/main/java/com/esprit/stageback/controllers/AdminGroupsController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AdminGroupDTO;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.GroupeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupsController {

    private final GroupeRepository groupeRepo;

    @GetMapping
    public ResponseEntity<List<AdminGroupDTO>> all() {
        List<AdminGroupDTO> out = groupeRepo.findAll().stream()
                .sorted((a, b) -> a.getNom().compareToIgnoreCase(b.getNom()))
                .map(g -> AdminGroupDTO.builder()
                        .id(g.getId())
                        .nom(g.getNom())
                        .specialite(g.getSpecialite())
                        .build())
                .toList();
        return ResponseEntity.ok(out);
    }
}
