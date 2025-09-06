// src/main/java/com/esprit/stageback/dto/trainer/ScheduleItemDTO.java
package com.esprit.stageback.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleItemDTO {
    private Long groupeId;
    private String groupeName;
    private String room;
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
}
