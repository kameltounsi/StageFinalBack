package com.esprit.stageback.entities;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Cours {
    @Id @GeneratedValue
    private Long id;

    private String titre;
    private String description;
    private String fichierUrl;

    private LocalDateTime dateDepot;

    @ManyToOne
    private User formateur;
}
