// src/main/java/com/esprit/stageback/dto/AdminGroupDTO.java
package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminGroupDTO {
    private Long id;
    private String nom;
    private String specialite;
}
