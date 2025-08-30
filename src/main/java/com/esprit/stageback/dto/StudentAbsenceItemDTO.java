// src/main/java/com/esprit/stageback/dto/StudentAbsenceItemDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class StudentAbsenceItemDTO {
    private Long presenceId;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private String matiere;
    private String groupeNom;
    private String salle;

    private Boolean justified;          // true/false/null
    private String justificationNote;   // raison éventuelle
}
