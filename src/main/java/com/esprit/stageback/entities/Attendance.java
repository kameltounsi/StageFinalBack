package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_session_student",
                columnNames = {"emploi_id", "student_id"})
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Séance (EmploiTemps)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emploi_id")
    private EmploiTemps emploi;

    // Etudiant
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    // Présent / absent
    @Column(nullable = false)
    private boolean present;

    @Column(nullable = false)
    private LocalDateTime markedAt;
}
