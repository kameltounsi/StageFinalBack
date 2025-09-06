package com.esprit.stageback.dto;

import com.esprit.stageback.entities.User;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainerWithGroupsDTO {
    private Long id;
    private String fullName;
    private String email;
    private String specialite;
    private List<GroupMiniDTO> groups;

    public static TrainerWithGroupsDTO fromEntity(User u) {
        return TrainerWithGroupsDTO.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .specialite(u.getSpecialite())
                .groups(
                        u.getTrainerGroupes() == null ? List.of() :
                                u.getTrainerGroupes().stream()
                                        .map(g -> new GroupMiniDTO(g.getId(), g.getNom()))
                                        .toList()
                )
                .build();
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GroupMiniDTO {
        private Long id;
        private String name;
    }
}
