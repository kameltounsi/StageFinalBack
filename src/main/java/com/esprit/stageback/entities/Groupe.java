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

    @ManyToMany
    @JoinTable(
            name = "groupe_trainers",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private List<User> trainers;

    @ManyToMany(mappedBy = "groupes")
    @JsonIgnore
    private List<User> etudiants;

    @OneToMany(mappedBy = "groupe")
    @JsonIgnore
    private List<EmploiTemps> emplois;
}
