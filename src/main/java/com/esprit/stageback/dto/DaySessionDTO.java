// src/main/java/com/esprit/stageback/dto/student/DaySessionDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DaySessionDTO {
    private LocalDateTime start;
    private LocalDateTime end;
    private String subject;
    private String room;
    private String trainerName;
}
