package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;
    private String profilePicture;
    private String specialite;

    // OneToMany est LAZY par défaut (on rend explicite et on exclut toString/equals)
    @OneToMany(mappedBy = "formateur", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"formateur"})
    private List<EmploiTemps> emploisDuTemps;

    @OneToMany(mappedBy = "formateur", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"formateur"})
    private List<Cours> coursDonnes;

    @OneToMany(mappedBy = "etudiant", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"etudiant"})
    private List<Note> notes;

    @OneToMany(mappedBy = "etudiant", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"etudiant"})
    private List<Presence> presences;

    // Était EAGER → mettre LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"students", "trainers"})
    private Groupe studentGroupe;

    // Était EAGER → mettre LAZY (ManyToMany est LAZY par défaut, on force explicitement)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainer_groupes",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"students", "trainers"})
    private List<Groupe> trainerGroupes;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_VISIBLE;

    private String resetCode;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + '\'' + ", email='" + email + '\'' + ", role=" + role + '}';
    }
}
