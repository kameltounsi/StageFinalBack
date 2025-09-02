// src/main/java/com/esprit/stageback/dto/SaveGradesRequest.java
package com.esprit.stageback.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaveGradesRequest {

    // <-- accepte "groupeId" et "groupId"
    @JsonAlias({"groupeId", "groupId"})
    private Long groupeId;

    private String matiere;
    private LocalDate date;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long studentId;
        private Double valeur;
        private String commentaire;
    }

    private List<Item> items;
}
