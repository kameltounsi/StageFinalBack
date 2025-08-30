// src/main/java/com/esprit/stageback/dto/StudentAbsenceSummaryDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class StudentAbsenceSummaryDTO {
    private long total;                       // total absences sur la période
    private long justified;                   // dont justifiées
    private long unJustified;                 // dont non justifiées
    private List<StudentAbsenceItemDTO> items;
}
