package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.services.GroupeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupeController {

    private final GroupeService groupeService;

    @PostMapping("/add")
    public ResponseEntity<Groupe> addGroup(
            @RequestParam String specialite,
            @RequestParam String niveau,
            @RequestParam(required = false) List<Long> trainerIds,
            @RequestParam(required = false) List<Long> studentIds) {

        Groupe groupe = groupeService.createGroup(specialite, niveau, trainerIds, studentIds);
        return ResponseEntity.ok(groupe);
    }

    @GetMapping
    public ResponseEntity<List<Groupe>> getAllGroups() {
        return ResponseEntity.ok(groupeService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Groupe> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupeService.getGroupById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupeService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
    // ✅ Nouvel endpoint spécifique pour ton besoin Angular
    @GetMapping("/by-specialite")
    public List<Groupe> getGroupsBySpecialiteAndLevel(
            @RequestParam String specialite,
            @RequestParam String level
    ) {
        return groupeService.findGroupsBySpecialiteAndLevel(specialite, level);
    }
}
