package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    private String profilePicture; // Optional
    @OneToMany(mappedBy = "formateur")
    @JsonIgnore
    private List<EmploiTemps> emploisDuTemps;

    @OneToMany(mappedBy = "formateur")
    @JsonIgnore
    private List<Cours> coursDonnes;

    @OneToMany(mappedBy = "etudiant")
    private List<Note> notes;

    @OneToMany(mappedBy = "etudiant")
    private List<Presence> presences;

    // === Relation pour STUDENT ===
    @ManyToOne
    @JoinColumn(name = "groupe_id")
    private Groupe studentGroupe; // un seul groupe si STUDENT

    // === Relation pour TRAINER ===
    @ManyToMany
    @JoinTable(
            name = "trainer_groupes",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private List<Groupe> trainerGroupes; // plusieurs groupes si TRAINER
    @Enumerated(EnumType.STRING)
    private Roles role;
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_VISIBLE; // valeur par défaut

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
    private String resetCode;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ",role='" + role + '\''+
                '}';
    }
}
