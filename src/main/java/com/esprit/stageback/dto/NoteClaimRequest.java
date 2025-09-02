// src/main/java/com/esprit/stageback/dto/NoteClaimRequest.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoteClaimRequest {
    private String matiere;          // required
    private String message;          // required
    private Double proposedCc;       // optional
    private Double proposedExamen;   // optional
}
