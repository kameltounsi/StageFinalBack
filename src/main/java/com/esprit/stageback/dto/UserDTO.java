package com.esprit.stageback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String profilePicture;

    // Student
    private String studentClass;
    private Long studentGroupId;

    // Trainer
    private List<String> trainerClasses;
    private List<Long> trainerGroupIds;
}
