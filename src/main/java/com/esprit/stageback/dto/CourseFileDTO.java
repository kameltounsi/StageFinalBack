// src/main/java/com/esprit/stageback/dto/CourseFileDTO.java
package com.esprit.stageback.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CourseFileDTO {
    Long id;
    String title;
    String contentType;
    Long sizeBytes;
    Instant createdAt;

    Long groupeId;
    String groupeName;

    String subject;

    // NEW: pour affichage “Trainer”
    Long trainerId;
    String trainerEmail;
    String presignedUrl; // rempli au besoin
}
