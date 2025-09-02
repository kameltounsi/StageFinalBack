// src/main/java/com/esprit/stageback/dto/ClaimDecisionRequest.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClaimDecisionRequest {
    /** Optional new grades when approving (both nullable — only provided fields are changed) */
    private Double newCc;       // 0..20 or null to keep current
    private Double newExamen;   // 0..20 or null to keep current
    /** Trainer message back to student */
    private String reply;       // required for reject, optional for approve
}
