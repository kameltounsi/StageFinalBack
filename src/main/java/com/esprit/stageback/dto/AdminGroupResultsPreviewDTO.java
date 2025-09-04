// src/main/java/com/esprit/stageback/dto/AdminGroupResultsPreviewDTO.java
package com.esprit.stageback.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminGroupResultsPreviewDTO {
    private Long groupId;
    private String groupName;
    private String specialite;

    private List<String> expectedSubjects;

    private List<AdminStudentResultDTO> students;

    private long admittedCount;
    private long refusedCount;
    private long incompleteCount;

    // vrai si aucun étudiant n'est "INCOMPLETE"
    private boolean allComplete;

    // nom du groupe cible déduit (ex: "WD B3") si applicable
    private String suggestedNextGroupName;
    private Long suggestedNextGroupId; // si trouvé en base
}
