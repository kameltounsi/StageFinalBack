package com.esprit.stageback.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupOptionDTO {
    private Long id;
    private String nom;
    private String specialite;
}
