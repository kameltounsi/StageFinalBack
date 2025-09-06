// src/main/java/com/esprit/stageback/dto/trainer/TrainerGroupRowDTO.java
package com.esprit.stageback.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerGroupRowDTO {
    private Long id;
    private String name;
    private String specialite;
    private String level;       // "A" | "B" | null
    private long studentCount;
}
