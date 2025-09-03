// src/main/java/com/esprit/stageback/dto/UploadCourseResponse.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UploadCourseResponse {
    private Long id;
    private String title;
    private Long groupeId;
    private String subject;
}
