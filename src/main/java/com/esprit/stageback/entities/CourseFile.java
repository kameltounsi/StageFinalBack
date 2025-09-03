// src/main/java/com/esprit/stageback/entities/CourseFile.java
package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "course_files")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;                // ex: “Chapitre 1.pdf”
    private String s3Key;                // ex: trainer/{trainerId}/{yyyy/MM}/uuid.pdf
    private String contentType;          // application/pdf
    private long sizeBytes;

    // Owner
    private Long trainerId;
    private String trainerEmail;

    // Ciblage pédagogique
    private Long groupeId;
    private String groupeName;           // cache du nom du groupe (affichage)
    private String subject;              // matière validée par spécialité

    private Instant createdAt;
}
