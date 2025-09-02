// src/main/java/com/esprit/stageback/dto/TrainerNoteRow.java
package com.esprit.stageback.dto;

import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerNoteRow {
    private Long studentId;
    private String studentName;
    private String studentEmail;

    private Double valeur;       // nullable if not set yet
    private String commentaire;  // nullable
    private String matiere;      // echo back
    private LocalDate date;      // echo back
}
