// src/main/java/com/esprit/stageback/entities/NoteClaim.java
package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoteClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Etudiant qui réclame */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    /** Facultatif: le tuteur ciblé si tu veux router vers un formateur précis */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    /** Matière concernée */
    private String matiere;

    /** Valeurs actuelles (snapshot à l’instant) */
    private Double cc;
    private Double examen;
    private Double moyenne;
    private Double weightCc;
    private Double weightExam;

    /** Proposition de correction éventuelle (facultative) */
    private Double proposedCc;
    private Double proposedExamen;

    /** Message/raison de la réclamation (obligatoire côté API) */
    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NoteClaimStatus status;

    /** Réponse du tuteur (lors du traitement) */
    @Column(length = 2000)
    private String tutorReply;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
