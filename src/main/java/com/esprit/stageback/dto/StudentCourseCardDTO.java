// src/main/java/com/esprit/stageback/dto/StudentCourseCardDTO.java
package com.esprit.stageback.dto;

import java.time.Instant;

public record StudentCourseCardDTO(
        Long id,
        String title,
        String subject,
        Long groupeId,
        String groupeName,
        String trainerName,
        String contentType,
        long sizeBytes,
        Instant createdAt
) {}
