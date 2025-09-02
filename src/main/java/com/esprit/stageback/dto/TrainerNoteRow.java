// src/main/java/com/esprit/stageback/dto/TrainerNoteRow.java
package com.esprit.stageback.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerNoteRow {
    private Long studentId;
    private String studentName;
    private String studentEmail;

    private Double valeur;
    private String commentaire;
    private String matiere;
}
