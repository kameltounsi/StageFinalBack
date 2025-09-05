// src/main/java/com/esprit/stageback/dto/AdminApplyResultsResponse.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminApplyResultsResponse {
    private Long   sourceGroupId;
    private Long   targetGroupId;
    private String targetGroupName;

    private long movedCount;   // promoted
    private long stayedCount;  // remained

    // Year rollover purge results
    private long purgedNotesCount;
    private long purgedClaimsCount;

    // NEW: capacity change on target group
    private int targetCapacityBefore;
    private int targetCapacityAfter;
}
