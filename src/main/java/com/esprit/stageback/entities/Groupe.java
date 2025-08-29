package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Groupe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private String specialite;
    private int trainerCapacity = 2;
    private int studentCapacity = 25;

    // Était EAGER → passer en LAZY
    @OneToMany(mappedBy = "studentGroupe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"studentGroupe", "trainerGroupes", "notes", "presences", "emploisDuTemps", "coursDonnes"})
    private List<User> students;

    // Était EAGER → passer en LAZY
    @ManyToMany(mappedBy = "trainerGroupes", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"studentGroupe", "trainerGroupes", "notes", "presences", "emploisDuTemps", "coursDonnes"})
    private List<User> trainers;

    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"groupe", "formateur"})
    private List<EmploiTemps> emplois;
}
