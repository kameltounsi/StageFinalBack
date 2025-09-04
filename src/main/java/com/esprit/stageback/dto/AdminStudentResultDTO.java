// src/main/java/com/esprit/stageback/dto/AdminStudentResultDTO.java
package com.esprit.stageback.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStudentResultDTO {
    private Long studentId;
    private String studentName;
    private String studentEmail;

    // moyenne par matière (matiere -> moyenne 0..20 ou null si non notée)
    private Map<String, Double> subjectAverages;

    // matières manquantes (non notées)
    private List<String> missingSubjects;

    // moyenne générale (sur matières attendues notées) ou null si incomplète
    private Double overall;

    // "ADMIS" | "REFUSE" | "INCOMPLETE"
    private String status;
}
