// src/main/java/com/esprit/stageback/dto/AdminApplyResultsResponse.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminApplyResultsResponse {
    private Long sourceGroupId;
    private Long targetGroupId;
    private String targetGroupName;

    private long movedCount;          // promoted
    private long stayedCount;         // remained in source group

    // NEW: what we purged right after publishing results
    private long purgedNotesCount;
    private long purgedClaimsCount;
}
