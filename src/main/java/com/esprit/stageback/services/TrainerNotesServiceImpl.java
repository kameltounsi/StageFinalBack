// src/main/java/com/esprit/stageback/services/TrainerNotesServiceImpl.java
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

    private Double clamp20(Double v) {
        if (v == null) return null;
        return Math.max(0d, Math.min(20d, v));
    }

    private Double round2(Double v) {
        if (v == null) return null;
        return Math.round(v * 100.0) / 100.0;
    }

    private Double computeAverage(Double cc, Double exam, double wCc, double wExam) {
        Double ccSafe = clamp20(cc);
        Double exSafe = clamp20(exam);
        if (ccSafe == null && exSafe == null) return null;
        if (ccSafe == null) return round2(exSafe);
        if (exSafe == null) return round2(ccSafe);
        return round2(ccSafe * wCc + exSafe * wExam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeRow> loadSheet(String trainerEmail, Long groupeId, String matiere) {
        assertTrainerInGroup(trainerEmail, groupeId);

        // Étudiants du groupe
        List<User> students = userRepo.findStudentsByGroupeId(groupeId);
        if (students.isEmpty()) return List.of();

        List<Long> studentIds = students.stream().map(User::getId).toList();

        // Notes existantes (par matière, sans date)
        List<Note> existing = noteRepo.findByEtudiant_IdInAndMatiere(studentIds, matiere);

        Map<Long, Note> byStudent = existing.stream()
                .collect(Collectors.toMap(n -> n.getEtudiant().getId(), n -> n, (a, b) -> a));

        // Construire les lignes
        List<GradeRow> rows = new ArrayList<>(students.size());
        for (User s : students) {
            Note n = byStudent.get(s.getId());
            rows.add(GradeRow.builder()
                    .studentId(s.getId())
                    .studentName(s.getFullName())
                    .studentEmail(s.getEmail())
                    .cc(n != null ? n.getCc() : null)
                    .examen(n != null ? n.getExamen() : null)
                    .moyenne(n != null ? n.getMoyenne() : null)
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
        if (payload.getItems() == null) payload.setItems(List.of());

        assertTrainerInGroup(trainerEmail, payload.getGroupeId());

        // Pondérations (fallback 40/60) + normalisation
        double wCc   = payload.getWeightCc()   != null ? payload.getWeightCc()   : 0.40;
        double wExam = payload.getWeightExam() != null ? payload.getWeightExam() : 0.60;
        if (Math.abs((wCc + wExam) - 1.0) > 1e-6) {
            double sum = wCc + wExam;
            if (sum <= 0) { wCc = 0.40; wExam = 0.60; }
            else { wCc /= sum; wExam /= sum; }
        }

        // Charger les users visés pour préparer l'upsert
        List<Long> ids = payload.getItems().stream()
                .map(SaveGradesRequest.Item::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, User> users = new HashMap<>();
        if (!ids.isEmpty()) {
            for (User u : userRepo.findAllById(ids)) users.put(u.getId(), u);
        }

        // Upsert note par (studentId, matiere)
        for (SaveGradesRequest.Item it : payload.getItems()) {
            if (it.getStudentId() == null) continue;
            User etu = users.get(it.getStudentId());
            if (etu == null) continue;

            Note note = noteRepo.findByEtudiant_IdAndMatiere(it.getStudentId(), payload.getMatiere())
                    .orElseGet(() -> Note.builder()
                            .etudiant(etu)
                            .matiere(payload.getMatiere())
                            .build());

            note.setCc(it.getCc());
            note.setExamen(it.getExamen());
            note.setWeightCc(wCc);
            note.setWeightExam(wExam);
            note.setMoyenne(computeAverage(it.getCc(), it.getExamen(), wCc, wExam));
            note.setCommentaire(it.getCommentaire());

            noteRepo.save(note);
        }
    }
}
