// src/main/java/com/esprit/stageback/dto/GradeRow.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GradeRow {
    private Long   studentId;
    private String studentName;
    private String studentEmail;

    // nouvelles colonnes
    private Double cc;        // 0..20 (nullable)
    private Double examen;    // 0..20 (nullable)
    private Double moyenne;   // calculée côté back/front
    private String commentaire;
}
