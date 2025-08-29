// src/main/java/com/esprit/stageback/dto/StudentWeeklyItemDto.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.EmploiTemps;
import java.time.LocalDate;
import java.time.LocalTime;

public record StudentWeeklyItemDto(
        Long id,
        LocalDate date,
        LocalTime heureDebut,
        LocalTime heureFin,
        String matiere,
        String salle,
        String formateurNom,   // NEW
        Long formateurId       // optional, handy for tooltips/links
) {
    public static StudentWeeklyItemDto fromEntity(EmploiTemps e) {
        String trainerName = null;
        Long trainerId = null;
        if (e.getFormateur() != null) {
            // pick what you actually have on User:
            // String fn = e.getFormateur().getFirstName();
            // String ln = e.getFormateur().getLastName();
            // trainerName = (fn != null ? fn : "") + " " + (ln != null ? ln : "");
            // OR if you already have a fullName getter:
            trainerName = e.getFormateur().getFullName();
            trainerId   = e.getFormateur().getId();
        }

        return new StudentWeeklyItemDto(
                e.getId(),
                e.getDate(),
                e.getHeureDebut(),
                e.getHeureFin(),
                e.getMatiere(),
                e.getSalle(),
                trainerName,
                trainerId
        );
    }
}
