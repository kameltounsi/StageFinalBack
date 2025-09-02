package com.esprit.stageback.services;

import com.esprit.stageback.dto.GradeRow;
import com.esprit.stageback.dto.SaveGradesRequest;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerNotesServiceImpl implements TrainerNotesService {

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;

    private void assertTrainerInGroup(String trainerEmail, Long groupeId) {
        List<Long> gids = userRepo.findTrainerGroupIdsByEmailIgnoreCase(trainerEmail);
        if (gids == null || gids.stream().noneMatch(id -> Objects.equals(id, groupeId))) {
            throw new SecurityException("You are not assigned to this group.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeRow> loadSheet(String trainerEmail, Long groupeId, LocalDate date, String matiere) {
        assertTrainerInGroup(trainerEmail, groupeId);

        // Étudiants du groupe
        List<User> students = userRepo.findStudentsByGroupeId(groupeId);
        if (students.isEmpty()) return List.of();

        List<Long> studentIds = students.stream().map(User::getId).toList();

        // Notes existantes
        List<Note> existing = noteRepo.findByEtudiant_IdInAndDateAndMatiereIgnoreCase(studentIds, date, matiere);

        Map<Long, Note> byStudent = existing.stream()
                .collect(Collectors.toMap(n -> n.getEtudiant().getId(), n -> n, (a,b)->a));

        // Feuille fusionnée
        List<GradeRow> rows = new ArrayList<>(students.size());
        for (User s : students) {
            Note n = byStudent.get(s.getId());
            rows.add(GradeRow.builder()
                    .studentId(s.getId())
                    .studentName(s.getFullName())
                    .studentEmail(s.getEmail())
                    .valeur(n != null ? n.getValeur() : null)
                    .commentaire(n != null ? n.getCommentaire() : null)
                    .build());
        }
        return rows;
    }

    @Override
    @Transactional
    public void saveBulk(String trainerEmail, SaveGradesRequest payload) {
        Objects.requireNonNull(payload, "payload is null");
        Objects.requireNonNull(payload.getGroupeId(), "groupeId is null");
        Objects.requireNonNull(payload.getMatiere(), "matiere is null");
        Objects.requireNonNull(payload.getDate(), "date is null");
        if (payload.getItems() == null) payload.setItems(List.of());

        assertTrainerInGroup(trainerEmail, payload.getGroupeId());

        // Récupérer tous les étudiants ciblés
        List<Long> ids = payload.getItems().stream()
                .map(SaveGradesRequest.Item::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (!ids.isEmpty()) {
            // Charger notes existantes
            List<Note> existing = noteRepo.findByEtudiant_IdInAndDateAndMatiereIgnoreCase(
                    ids, payload.getDate(), payload.getMatiere());

            Map<Long, Note> byStudent = existing.stream()
                    .collect(Collectors.toMap(n -> n.getEtudiant().getId(), n -> n, (a,b)->a));

            // Map des Users (pour créer les nouvelles notes)
            Map<Long, User> users = new HashMap<>();
            if (!ids.isEmpty()) {
                List<User> us = userRepo.findAllById(ids);
                for (User u : us) users.put(u.getId(), u);
            }

            // Upsert
            for (SaveGradesRequest.Item item : payload.getItems()) {
                if (item.getStudentId() == null) continue;

                Note note = byStudent.get(item.getStudentId());
                if (note == null) {
                    // créer
                    User etu = users.get(item.getStudentId());
                    if (etu == null) continue; // sécurité
                    note = Note.builder()
                            .etudiant(etu)
                            .matiere(payload.getMatiere())
                            .date(payload.getDate())
                            .valeur(item.getValeur())
                            .commentaire(item.getCommentaire())
                            .build();
                } else {
                    // maj
                    note.setValeur(item.getValeur());
                    note.setCommentaire(item.getCommentaire());
                }
                noteRepo.save(note);
            }
        }
    }
}
