package com.esprit.stageback.view;
import lombok.*;
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class SeanceView {
    private String heureDebut;
    private String heureFin;
    private String matiere;
    private String salle;
    private String formateurNom;
}