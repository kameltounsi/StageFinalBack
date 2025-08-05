package com.esprit.stageback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String profilePicture;
    private String studentClass; // pour Student
    private List<String> trainerClasses; // pour Trainer
}
