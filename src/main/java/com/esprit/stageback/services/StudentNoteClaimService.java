// src/main/java/com/esprit/stageback/services/StudentNoteClaimService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.NoteClaimDTO;
import com.esprit.stageback.dto.NoteClaimRequest;

import java.util.List;

public interface StudentNoteClaimService {
    NoteClaimDTO submitClaim(String studentEmail, NoteClaimRequest req);
    List<NoteClaimDTO> myClaims(String studentEmail);
}
