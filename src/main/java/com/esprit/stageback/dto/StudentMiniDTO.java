// src/main/java/com/esprit/stageback/dto/trainer/StudentMiniDTO.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentMiniDTO {
    private Long id;
    private String fullName;
    private String email;
    private String profilePicture;
}
