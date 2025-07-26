package com.esprit.stageback.entities;

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
public class EmploiTemps {
    @Id @GeneratedValue
    private Long id;

    private LocalDate date;
    private String heure;
    private String salle;

    @ManyToOne
    private User formateur;

    @ManyToOne
    private Groupe groupe;
}
