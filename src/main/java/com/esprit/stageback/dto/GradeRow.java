package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeRow {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Double valeur;        // null si pas encore saisie
    private String commentaire;   // null/"" si vide
}
