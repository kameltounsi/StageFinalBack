// src/main/java/com/esprit/stageback/dto/StudentNoteDTO.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentNoteDTO {
    private String matiere;
    private Double cc;          // 0..20 ou null
    private Double examen;      // 0..20 ou null
    private Double moyenne;     // 0..20 ou null
    private Double weightCc;    // ex: 0.4
    private Double weightExam;  // ex: 0.6
    private String commentaire; // optionnel
}
