package com.esprit.stageback.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EmploiTempsDTO {
    private Long id;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String salle;
    private String matiere;
    private String formateurNom; // Nom complet du formateur
    private String groupeNom;    // Nom du groupe
}
