// src/main/java/com/esprit/stageback/dto/EmploiOptionDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmploiOptionDTO {
    private Long id;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Long groupeId;
    private String groupeNom;
    private String matiere;
    private String salle;
}
