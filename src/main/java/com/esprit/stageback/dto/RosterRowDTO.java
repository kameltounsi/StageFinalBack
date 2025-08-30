// src/main/java/com/esprit/stageback/dto/RosterRowDTO.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.StatutPresence;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RosterRowDTO {
    private Long studentId;
    private String fullName;
    private String email;                  // ⬅️ (optionnel si tu l’as)
    private StatutPresence current;        // PRESENT/ABSENT/RETARD (retard non utilisé ici)
    private Boolean justified;             // ⬅️ NEW
    private String justificationNote;      // ⬅️ NEW
}
