// src/main/java/com/esprit/stageback/services/StudentNoteClaimServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.NoteClaimRequest;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.NoteClaim;
import com.esprit.stageback.entities.NoteClaimStatus;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.NoteClaimRepository;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentNoteClaimServiceImpl implements StudentNoteClaimService {

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final NoteClaimRepository claimRepo;

    @Override
    @Transactional
    public NoteClaimDTO submitClaim(String studentEmail, NoteClaimRequest req) {
        if (req == null || req.getMatiere() == null || req.getMatiere().isBlank()) {
            throw new IllegalArgumentException("Subject (matiere) is required.");
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message is required.");
        }

        User student = userRepo.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        // On récupère la note actuelle (si existe) pour snapshot
        Note note = noteRepo.findByEtudiant_EmailIgnoreCaseOrderByMatiereAsc(studentEmail).stream()
                .filter(n -> req.getMatiere().equalsIgnoreCase(n.getMatiere()))
                .findFirst()
                .orElse(null);

        NoteClaim claim = NoteClaim.builder()
                .student(student)
                .tutor(null)
                .matiere(req.getMatiere())
                .message(req.getMessage())
                .proposedCc(req.getProposedCc())
                .proposedExamen(req.getProposedExamen())
                .status(NoteClaimStatus.PENDING)
                .cc(note != null ? note.getCc() : null)
                .examen(note != null ? note.getExamen() : null)
                .moyenne(note != null ? note.getMoyenne() : null)
                .weightCc(note != null ? note.getWeightCc() : null)
                .weightExam(note != null ? note.getWeightExam() : null)
                .build();

        claim = claimRepo.save(claim);

        return toDto(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteClaimDTO> myClaims(String studentEmail) {
        return claimRepo.findByStudent_EmailIgnoreCaseOrderByCreatedAtDesc(studentEmail)
                .stream().map(this::toDto).toList();
    }

    private NoteClaimDTO toDto(NoteClaim c) {
        return NoteClaimDTO.builder()
                .id(c.getId())
                .matiere(c.getMatiere())
                .message(c.getMessage())
                .cc(c.getCc())
                .examen(c.getExamen())
                .moyenne(c.getMoyenne())
                .weightCc(c.getWeightCc())
                .weightExam(c.getWeightExam())
                .proposedCc(c.getProposedCc())
                .proposedExamen(c.getProposedExamen())
                .status(c.getStatus())
                .tutorReply(c.getTutorReply())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
