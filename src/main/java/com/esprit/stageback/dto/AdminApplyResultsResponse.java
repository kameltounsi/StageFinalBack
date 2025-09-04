// src/main/java/com/esprit/stageback/dto/AdminApplyResultsResponse.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminApplyResultsResponse {
    private Long sourceGroupId;
    private Long targetGroupId;
    private String targetGroupName;

    private long movedCount;     // nombre d'étudiants promus
    private long stayedCount;    // refusés (ou incomplets)
}
