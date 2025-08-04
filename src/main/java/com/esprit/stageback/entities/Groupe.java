package com.esprit.stageback.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Groupe {
    @Id @GeneratedValue
    private Long id;

    private String nom;
    private String specialite; // Informatique, BTP, Commerce...

    // ✅ Nouvelle capacité par défaut
    private int trainerCapacity = 2;
    private int studentCapacity = 25;

    @OneToMany(mappedBy = "studentGroupe")
    @JsonIgnore
    private List<User> students;

    @ManyToMany(mappedBy = "trainerGroupes")
    @JsonIgnore
    private List<User> trainers;

    @OneToMany(mappedBy = "groupe")
    @JsonIgnore
    private List<EmploiTemps> emplois;
}
