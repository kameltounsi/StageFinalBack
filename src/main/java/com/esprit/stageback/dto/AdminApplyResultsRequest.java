// src/main/java/com/esprit/stageback/dto/AdminApplyResultsRequest.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminApplyResultsRequest {
    private Long groupeId;
    /** Optional explicit target group; if absent we derive from level name (A → B) and create if missing */
    private Long targetGroupId;
}
