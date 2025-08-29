// src/main/java/com/esprit/stageback/dto/WeeklyItemDto.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyItemDto {
    private Long id;
    private String date;       // "YYYY-MM-DD"
    private String heureDebut; // "HH:mm"
    private String heureFin;   // "HH:mm"
    private String matiere;
    private String salle;

    // Pour le front : il lit soit groupeNom, soit groupe.nom/specialite
    private String groupeNom;
    private GroupeShortDto groupe;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GroupeShortDto {
        private Long id;
        private String nom;
        private String specialite;
    }

    private static final DateTimeFormatter D = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");

    public static WeeklyItemDto fromEntity(EmploiTemps e) {
        Groupe g = e.getGroupe();
        String nom = null;
        if (g != null) {
            nom = (g.getNom() != null && !g.getNom().isBlank())
                    ? g.getNom() : g.getSpecialite();
        }

        return WeeklyItemDto.builder()
                .id(e.getId())
                .date(e.getDate() != null ? e.getDate().format(D) : null)
                .heureDebut(e.getHeureDebut() != null ? e.getHeureDebut().format(T) : null)
                .heureFin(e.getHeureFin() != null ? e.getHeureFin().format(T) : null)
                .matiere(e.getMatiere())
                .salle(e.getSalle())
                .groupeNom(nom)
                .groupe(g != null ? GroupeShortDto.builder()
                        .id(g.getId())
                        .nom(g.getNom())
                        .specialite(g.getSpecialite())
                        .build() : null)
                .build();
    }
}
