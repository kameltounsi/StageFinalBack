// src/main/java/com/esprit/stageback/dto/student/StudentOverviewDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentOverviewDTO {
    private String studentName;

    private Long groupId;
    private String groupName;
    private String specialite;
    private Double average;               // moyenne générale (0-20) — placeholder
    private Integer attendedSessions;     // placeholder
    private Integer missedSessions;       // placeholder
    private Integer pendingClaims;        // placeholder
    private Integer unreadMessages;       // placeholder

    private NextSessionDTO nextSession;   // prochain cours (si dispo)

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NextSessionDTO {
        private LocalDateTime start;
        private LocalDateTime end;
        private String subject;
        private String room;
        private String trainerName;
    }
}
