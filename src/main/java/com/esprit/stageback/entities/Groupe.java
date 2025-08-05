package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String specialite; // Exemple: Informatique, BTP, Commerce...

    // ✅ Capacités par défaut
    private int trainerCapacity = 2;
    private int studentCapacity = 25;

    // Liste des étudiants
    @OneToMany(mappedBy = "studentGroupe", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"studentGroupe", "trainerGroupes", "notes", "presences", "emploisDuTemps", "coursDonnes"})
    private List<User> students;

    // Liste des formateurs
    @ManyToMany(mappedBy = "trainerGroupes", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"studentGroupe", "trainerGroupes", "notes", "presences", "emploisDuTemps", "coursDonnes"})
    private List<User> trainers;

    // Emplois du temps liés au groupe
    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"groupe", "formateur"})
    private List<EmploiTemps> emplois;
}
