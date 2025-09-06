// src/main/java/com/esprit/stageback/dto/trainer/TrainerOverviewDTO.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerOverviewDTO {
    private int myGroups;               // how many groups I teach
    private long myStudents;            // sum of students across my groups
    private int sessionsThisWeek;       // optional (0 if you don't track sessions)
    private int pendingClaims;          // optional (0 if you don't track claims)
    private int ungradedSubmissions;    // optional (0 if you don't track submissions)
}
