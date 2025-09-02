// src/main/java/com/esprit/stageback/entities/Note.java
package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "note",
        uniqueConstraints = {
                // 1 seule ligne par étudiant + matière
                @UniqueConstraint(name = "uk_note_student_matiere", columnNames = {"etudiant_id", "matiere"})
        }
)
public class Note {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String matiere;

    // 2 composantes (0..20)
    private Double cc;       // contrôle continu
    private Double examen;   // note d’examen

    // pondération choisie par le formateur (ex: 0.40 / 0.60 ou 0.20 / 0.80)
    @Column(nullable = false)
    private Double weightCc = 0.40;

    @Column(nullable = false)
    private Double weightExam = 0.60;

    // moyenne calculée et stockée (convenience + reporting)
    private Double moyenne;

    private String commentaire;

    // Optionnel: date “session” (tu peux la garder si utile)
    private LocalDate date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User etudiant;
}
