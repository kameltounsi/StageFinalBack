package com.esprit.stageback.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String specialite;

    private String profilePicture;

    private String status = "PENDING"; // PENDING | APPROVED | REJECTED
}
