// src/main/java/com/esprit/stageback/services/StudentNotesServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.StudentNoteDTO;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentNotesServiceImpl implements StudentNotesService {

    private final NoteRepository noteRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<StudentNoteDTO> myNotes(String studentEmail) {
        // On récupère l'étudiant pour sécuriser (et pouvoir étendre plus tard)
        User student = userRepo.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalStateException("Student not found"));

        List<Note> notes = noteRepo.findByEtudiant_IdOrderByMatiereAsc(student.getId());

        return notes.stream()
                // tri stable par nom de matière (déjà trié par repo, mais au cas où)
                .sorted(Comparator.comparing(n -> n.getMatiere() == null ? "" : n.getMatiere().toLowerCase()))
                .map(n -> StudentNoteDTO.builder()
                        .matiere(n.getMatiere())
                        .cc(n.getCc())
                        .examen(n.getExamen())
                        .moyenne(n.getMoyenne())
                        .weightCc(n.getWeightCc())
                        .weightExam(n.getWeightExam())
                        .commentaire(n.getCommentaire())
                        .build())
                .toList();
    }
}
