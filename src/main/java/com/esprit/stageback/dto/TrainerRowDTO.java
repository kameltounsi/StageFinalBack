package com.esprit.stageback.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerRowDTO {
    private Long id;
    private String fullName;
    private String email;
    private String profilePicture;
    private List<GroupRefDTO> groups;
}
