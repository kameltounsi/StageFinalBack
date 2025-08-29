// src/main/java/com/esprit/stageback/dto/RosterRowDTO.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.StatutPresence;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RosterRowDTO {
    private Long studentId;
    private String fullName;
    private StatutPresence current; // null si non saisi
}
