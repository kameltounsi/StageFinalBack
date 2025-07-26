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
public class Presence {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User etudiant;

    @ManyToOne
    private Seance seance;

    @Enumerated(EnumType.STRING)
    private StatutPresence statut; // PRESENT, ABSENT, RETARD
}
