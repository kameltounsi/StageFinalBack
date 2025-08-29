package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.GroupeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupeController {

    private final GroupeService groupeService;
    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Groupe> addGroup(
            @RequestParam String specialite,
            @RequestParam String niveau, // keep "niveau" since your service expects it
            @RequestParam(required = false) List<Long> trainerIds,
            @RequestParam(required = false) List<Long> studentIds) {

        Groupe groupe = groupeService.createGroup(specialite, niveau, trainerIds, studentIds);
        return ResponseEntity.ok(groupe);
    }

    // ---- SINGLE listing endpoint (avoid duplicate /api/groups GET)
    @GetMapping
    public ResponseEntity<List<Groupe>> getAll() {
        return ResponseEntity.ok(groupeService.getAllGroups());
    }

    // ---- SINGLE by-id endpoint (avoid duplicate /api/groups/{id} GET)
    @GetMapping("/{id}")
    public ResponseEntity<Groupe> getById(@PathVariable Long id) {
        return groupeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupeService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // Filter by specialité + level (note: your addGroup uses "niveau"; here you used "level")
    // Keep API stable: either rename param to "niveau" or convert inside service.
    @GetMapping("/by-specialite")
    public ResponseEntity<List<Groupe>> getGroupsBySpecialiteAndLevel(
            @RequestParam String specialite,
            @RequestParam(name = "level") String niveau // map "level" -> "niveau"
    ) {
        return ResponseEntity.ok(groupeService.findGroupsBySpecialiteAndLevel(specialite, niveau));
    }

    @PostMapping("/{groupId}/add-student/{studentId}")
    public ResponseEntity<?> addStudentToGroup(
            @PathVariable Long groupId,
            @PathVariable Long studentId) {

        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!Objects.equals(group.getSpecialite(), student.getSpecialite())) {
            return ResponseEntity.badRequest().body("Speciality does not match group");
        }
        if (group.getStudentCapacity() <= 0) {
            return ResponseEntity.badRequest().body("No more student capacity available");
        }

        student.setStudentGroupe(group);
        userRepository.save(student);

        group.setStudentCapacity(Math.max(0, group.getStudentCapacity() - 1));
        groupeRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/add-trainer/{trainerId}")
    public ResponseEntity<Map<String, String>> addTrainerToGroup(
            @PathVariable Long groupId,
            @PathVariable Long trainerId) {

        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (!Objects.equals(group.getSpecialite(), trainer.getSpecialite())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Speciality does not match group"));
        }
        if (group.getTrainers().stream().anyMatch(t -> t.getId().equals(trainerId))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trainer already assigned to this group"));
        }
        if (group.getTrainers().size() >= group.getTrainerCapacity()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Group has reached the maximum trainer capacity"));
        }

        group.getTrainers().add(trainer);
        if (trainer.getTrainerGroupes() == null) {
            trainer.setTrainerGroupes(new ArrayList<>());
        }
        trainer.getTrainerGroupes().add(group);

        group.setTrainerCapacity(Math.max(0, group.getTrainerCapacity() - 1));

        groupeRepository.save(group);
        userRepository.save(trainer);

        return ResponseEntity.ok(Map.of("message", "Trainer added successfully"));
    }

    @DeleteMapping("/{groupId}/remove-student/{studentId}")
    public ResponseEntity<?> removeStudentFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long studentId) {

        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        group.setStudents(
                group.getStudents().stream()
                        .filter(s -> !s.getId().equals(studentId))
                        .toList()
        );
        group.setStudentCapacity(group.getStudentCapacity() + 1);

        student.setStudentGroupe(null);
        userRepository.save(student);
        groupeRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Student removed successfully"));
    }

    @DeleteMapping("/{groupId}/remove-trainer/{trainerId}")
    public ResponseEntity<?> removeTrainerFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long trainerId) {

        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        group.setTrainers(
                group.getTrainers().stream()
                        .filter(t -> !t.getId().equals(trainerId))
                        .toList()
        );
        group.setTrainerCapacity(group.getTrainerCapacity() + 1);

        if (trainer.getTrainerGroupes() != null) {
            trainer.getTrainerGroupes().remove(group);
            userRepository.save(trainer);
        }
        groupeRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Trainer removed successfully"));
    }

    @GetMapping("/available-students")
    public ResponseEntity<List<User>> getAvailableStudents(@RequestParam String specialite) {
        return ResponseEntity.ok(userRepository.findAvailableStudentsBySpecialite(specialite));
    }

    @GetMapping("/available-trainers")
    public ResponseEntity<List<User>> getAvailableTrainers(@RequestParam String specialite) {
        return ResponseEntity.ok(userRepository.findAvailableTrainersBySpecialite(specialite));
    }
}
