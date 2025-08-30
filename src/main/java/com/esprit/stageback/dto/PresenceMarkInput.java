// src/main/java/com/esprit/stageback/dto/PresenceMarkInput.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.StatutPresence;
import lombok.Data;

@Data
public class PresenceMarkInput {
    private Long studentId;
    private StatutPresence statut;      // PRESENT ou ABSENT
    private Boolean justified;          // ⬅️ NEW (optionnel – pertinent si ABSENT)
    private String justificationNote;   // ⬅️ NEW (optionnel)
}
