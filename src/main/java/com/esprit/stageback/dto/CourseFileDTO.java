// src/main/java/com/esprit/stageback/dto/CourseFileDTO.java
package com.esprit.stageback.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseFileDTO {
    private Long id;
    private String title;
    private String contentType;
    private long sizeBytes;
    private Instant createdAt;

    private Long groupeId;
    private String groupeName;
    private String subject;

    private String presignedUrl; // rempli au besoin
}
