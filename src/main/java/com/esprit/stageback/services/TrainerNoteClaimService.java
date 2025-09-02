// src/main/java/com/esprit/stageback/services/TrainerNoteClaimService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.ClaimDecisionRequest;

import java.util.List;

public interface TrainerNoteClaimService {
    List<NoteClaimDTO> inbox(String trainerEmail); // pending claims visible for this trainer
    NoteClaimDTO approve(String trainerEmail, Long claimId, ClaimDecisionRequest req);
    NoteClaimDTO reject(String trainerEmail, Long claimId, ClaimDecisionRequest req);
}
