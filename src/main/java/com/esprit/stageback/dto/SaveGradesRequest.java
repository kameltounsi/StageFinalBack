// src/main/java/com/esprit/stageback/dto/SaveGradesRequest.java
package com.esprit.stageback.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SaveGradesRequest {

    // accepte "groupeId" ou "groupId"
    @JsonAlias({"groupeId", "groupId"})
    private Long   groupeId;

    private String matiere;

    // pondérations (ex: 0.40 / 0.60 ou 0.20 / 0.80)
    private Double weightCc;     // optionnel (fallback 0.40)
    private Double weightExam;   // optionnel (fallback 0.60)

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Item {
        private Long   studentId;
        private Double cc;          // 0..20 (nullable)
        private Double examen;      // 0..20 (nullable)
        private String commentaire; // optionnel
    }

    private List<Item> items;
}
