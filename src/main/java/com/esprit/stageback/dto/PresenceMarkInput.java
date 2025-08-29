// src/main/java/com/esprit/stageback/dto/PresenceMarkInput.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.StatutPresence;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceMarkInput {
    private Long studentId;
    private StatutPresence statut; // PRESENT ou ABSENT
}
