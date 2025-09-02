// src/main/java/com/esprit/stageback/dto/NoteClaimDTO.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.NoteClaimStatus;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoteClaimDTO {
    private Long id;
    private String matiere;
    private String message;
    private Double cc;
    private Double examen;
    private Double moyenne;
    private Double weightCc;
    private Double weightExam;
    private Double proposedCc;
    private Double proposedExamen;
    private NoteClaimStatus status;
    private String tutorReply;
    private Instant createdAt;
    private Instant updatedAt;
}
