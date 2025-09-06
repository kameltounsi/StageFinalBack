// src/main/java/com/esprit/stageback/dto/student/GradeRowDTO.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeRowDTO {
    private String subject;
    private Double average;           // 0-20
    private Integer doneControls;     // rendus/contrôles faits
    private Integer totalControls;    // nb total de contrôles
    private String status;            // OK / MISSING / LATE ...
}
