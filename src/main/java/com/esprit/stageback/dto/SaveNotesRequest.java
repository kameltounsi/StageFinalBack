// src/main/java/com/esprit/stageback/dto/SaveNotesRequest.java
package com.esprit.stageback.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SaveNotesRequest {
    private Long groupId;
    private String matiere;
    private LocalDate date; // ISO yyyy-MM-dd
    private List<Item> items;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long studentId;
        private Double valeur;      // e.g. 0..20
        private String commentaire; // optional
    }
}
