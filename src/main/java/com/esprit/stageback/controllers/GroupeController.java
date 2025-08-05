package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.GroupeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PostMapping("/{groupId}/add-student/{studentId}")
    public ResponseEntity<?> addStudentToGroup(
            @PathVariable Long groupId,
            @PathVariable Long studentId) {
        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!group.getSpecialite().equals(student.getSpecialite())) {
            return ResponseEntity.badRequest().body("Speciality does not match group");
        }

        if (group.getStudentCapacity() <= 0) {
            return ResponseEntity.badRequest().body("No more student capacity available");
        }

        student.setStudentGroupe(group);
        userRepository.save(student);

        group.setStudentCapacity(group.getStudentCapacity() - 1);
        groupeRepository.save(group);

        return ResponseEntity.ok(group); // retourne group mis à jour
    }
    @PostMapping("/{groupId}/add-trainer/{trainerId}")
    public ResponseEntity<Map<String, String>> addTrainerToGroup(
            @PathVariable Long groupId,
            @PathVariable Long trainerId) {

        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        // Vérifier la spécialité
        if (!group.getSpecialite().equals(trainer.getSpecialite())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Speciality does not match group"));
        }

        // Vérifier si déjà ajouté
        if (group.getTrainers().stream().anyMatch(t -> t.getId().equals(trainerId))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trainer already assigned to this group"));
        }

        // Vérifier la capacité restante
        if (group.getTrainers().size() >= group.getTrainerCapacity()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Group has reached the maximum trainer capacity"));
        }

        // Ajouter le trainer
        group.getTrainers().add(trainer);

        // S'assurer que la liste des groupes du trainer existe
        if (trainer.getTrainerGroupes() == null) {
            trainer.setTrainerGroupes(new ArrayList<>());
        }

        trainer.getTrainerGroupes().add(group);

        // Décrémenter la capacité
        group.setTrainerCapacity(group.getTrainerCapacity() - 1);

        groupeRepository.save(group);
        userRepository.save(trainer);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Trainer added successfully");
        return ResponseEntity.ok(response);
    }

    // Supprimer un étudiant
    @DeleteMapping("/{groupId}/remove-student/{studentId}")
    public ResponseEntity<?> removeStudentFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long studentId) {
        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Retirer l'étudiant
        group.setStudents(
                group.getStudents().stream()
                        .filter(s -> !s.getId().equals(studentId))
                        .toList()
        );

        // 🔥 Libérer la place
        group.setStudentCapacity(group.getStudentCapacity() + 1);

        // ⚡ Supprimer le lien du côté étudiant
        student.setStudentGroupe(null);
        userRepository.save(student);

        groupeRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Student removed successfully"));
    }

    // Supprimer un formateur
    @DeleteMapping("/{groupId}/remove-trainer/{trainerId}")
    public ResponseEntity<?> removeTrainerFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long trainerId) {
        Groupe group = groupeRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        // Retirer le trainer
        group.setTrainers(
                group.getTrainers().stream()
                        .filter(t -> !t.getId().equals(trainerId))
                        .toList()
        );

        // 🔥 Libérer une place
        group.setTrainerCapacity(group.getTrainerCapacity() + 1);

        // ⚡ Supprimer le lien du côté formateur
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
