package com.esprit.stageback.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainersBySpecialiteDTO {
    private String specialite;
    private List<TrainerRowDTO> trainers;
}
