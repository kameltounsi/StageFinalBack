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
public class Note {
    @Id @GeneratedValue
    private Long id;

    private String matiere;
    private Double valeur;
    private String commentaire;
    private LocalDate date;

    @ManyToOne
    private User etudiant;
}
