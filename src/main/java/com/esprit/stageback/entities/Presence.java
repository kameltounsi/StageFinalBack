// src/main/java/com/esprit/stageback/entities/Presence.java
package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "presence",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_presence_student_emploi",
                columnNames = {"etudiant_id","emploi_temps_id"}
        )
)
public class Presence {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User etudiant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_temps_id")
    private EmploiTemps emploiTemps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPresence statut; // PRESENT, ABSENT, RETARD

    // ✅ Ajoute/garantis ces colonnes
    @Column(name = "justified")
    private Boolean justified;              // null/false/true

    @Column(name = "justification_note", length = 1000)
    private String justificationNote;       // texte libre quand absent
}
