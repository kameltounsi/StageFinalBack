package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class GroupStatusRowDTO {
    private Long id;
    private String name;
    private String specialite;
    private String level; // "A", "B", or null
    private int studentCount;
    private int studentCapacityLeft;
    private int trainerCapacityLeft;
    private int pendingClaims;
    private boolean gradingComplete;
    private int admittedCount;
    private int rejectedCount;
    private int incompleteCount;
}
