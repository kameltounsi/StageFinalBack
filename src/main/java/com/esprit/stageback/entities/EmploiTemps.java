package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class EmploiTemps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ important pour MySQL
    private Long id;

    private LocalDate date;           // Jour du cours
    private LocalTime heureDebut;     // Heure de début
    private LocalTime heureFin;       // Heure de fin
    private String salle;             // Salle du cours

    private String matiere;           // Matière (lié à la spécialité)

    @ManyToOne
    @JsonIgnoreProperties({"studentGroupe", "trainerGroupes", "password", "role"}) // on ignore 'role'
    private User formateur;

    @ManyToOne
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;            // Groupe (classe)
}
