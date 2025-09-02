// src/main/java/com/esprit/stageback/services/TrainerNoteClaimServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.ClaimDecisionRequest;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.NoteClaim;
import com.esprit.stageback.entities.NoteClaimStatus;
import com.esprit.stageback.repositories.NoteClaimRepository;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrainerNoteClaimServiceImpl implements TrainerNoteClaimService {

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final NoteClaimRepository claimRepo;

    private Double clamp20(Double v) {
        if (v == null) return null;
        return Math.max(0d, Math.min(20d, v));
    }

    private Double round2(Double v) {
        if (v == null) return null;
        return Math.round(v * 100.0) / 100.0;
    }

    private Double computeAvg(Double cc, Double ex, Double wCc, Double wEx) {
        Double ccS = clamp20(cc);
        Double exS = clamp20(ex);
        if (ccS == null && exS == null) return null;
        if (ccS == null) return round2(exS);
        if (exS == null) return round2(ccS);
        return round2(ccS * (wCc != null ? wCc : .4) + exS * (wEx != null ? wEx : .6));
    }

    /** Security: trainer must be assigned to student's group */
    private void assertTrainerCanSeeClaim(String trainerEmail, NoteClaim claim) {
        // Trainer groups
        List<Long> trainerGroupIds = userRepo.findTrainerGroupIdsByEmailIgnoreCase(trainerEmail);
        // Student group id (Optional -> Long)
        Long studentGroupId = userRepo.findStudentGroupIdByUserId(claim.getStudent().getId()).orElse(null);

        if (trainerGroupIds == null || studentGroupId == null ||
                trainerGroupIds.stream().noneMatch(id -> Objects.equals(id, studentGroupId))) {
            throw new SecurityException("You are not assigned to this student's group.");
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<NoteClaimDTO> inbox(String trainerEmail) {
        List<NoteClaim> pending = claimRepo.findByStatusOrderByCreatedAtDesc(NoteClaimStatus.PENDING);

        // Filter by trainer groups
        List<Long> trainerGroups = userRepo.findTrainerGroupIdsByEmailIgnoreCase(trainerEmail);
        if (trainerGroups == null || trainerGroups.isEmpty()) return List.of();

        return pending.stream()
                .filter(c -> {
                    Long studentGroupId = userRepo.findStudentGroupIdByUserId(c.getStudent().getId()).orElse(null);
                    return studentGroupId != null &&
                            trainerGroups.stream().anyMatch(gid -> Objects.equals(gid, studentGroupId));
                })
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NoteClaimDTO approve(String trainerEmail, Long claimId, ClaimDecisionRequest req) {
        NoteClaim c = claimRepo.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Claim not found"));
        assertTrainerCanSeeClaim(trainerEmail, c);
        if (c.getStatus() != NoteClaimStatus.PENDING)
            throw new IllegalStateException("Claim already processed.");

        // Upsert Note for student+subject
        Note note = noteRepo.findByEtudiant_IdAndMatiere(c.getStudent().getId(), c.getMatiere())
                .orElseGet(() -> Note.builder()
                        .etudiant(c.getStudent())
                        .matiere(c.getMatiere())
                        .weightCc(c.getWeightCc() != null ? c.getWeightCc() : .4)
                        .weightExam(c.getWeightExam() != null ? c.getWeightExam() : .6)
                        .build());

        // Apply new values if provided
        Double newCc = (req != null) ? req.getNewCc() : null;
        Double newEx = (req != null) ? req.getNewExamen() : null;
        if (newCc != null) note.setCc(clamp20(newCc));
        if (newEx != null) note.setExamen(clamp20(newEx));

        // Ensure weights exist
        if (note.getWeightCc() == null || note.getWeightExam() == null) {
            note.setWeightCc(c.getWeightCc() != null ? c.getWeightCc() : .4);
            note.setWeightExam(c.getWeightExam() != null ? c.getWeightExam() : .6);
        }

        // Recompute moyenne
        note.setMoyenne(computeAvg(note.getCc(), note.getExamen(), note.getWeightCc(), note.getWeightExam()));
        noteRepo.save(note);

        // Close claim
        c.setStatus(NoteClaimStatus.APPROVED);
        if (req != null && req.getReply() != null && !req.getReply().isBlank()) {
            c.setTutorReply(req.getReply().trim());
        }
        claimRepo.save(c);
        return toDto(c);
    }

    @Override
    @Transactional
    public NoteClaimDTO reject(String trainerEmail, Long claimId, ClaimDecisionRequest req) {
        NoteClaim c = claimRepo.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Claim not found"));
        assertTrainerCanSeeClaim(trainerEmail, c);
        if (c.getStatus() != NoteClaimStatus.PENDING)
            throw new IllegalStateException("Claim already processed.");

        if (req == null || req.getReply() == null || req.getReply().isBlank()) {
            throw new IllegalArgumentException("Reply message is required for rejection.");
        }
        c.setStatus(NoteClaimStatus.REJECTED);
        c.setTutorReply(req.getReply().trim());
        claimRepo.save(c);
        return toDto(c);
    }
}
