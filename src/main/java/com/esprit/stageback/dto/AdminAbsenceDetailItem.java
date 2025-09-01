// src/main/java/com/esprit/stageback/dto/AdminAbsenceDetailItem.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.StatutPresence;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminAbsenceDetailItem {
    private Long presenceId;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private String matiere;
    private String room;
    private StatutPresence statut;     // pour info
}
