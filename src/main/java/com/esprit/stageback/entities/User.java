package com.esprit.stageback.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    private String profilePicture;

    private String specialite;

    // Relations avec emploi du temps, cours, notes et présences
    @OneToMany(mappedBy = "formateur")
    @JsonIgnoreProperties({"formateur"})
    private List<EmploiTemps> emploisDuTemps;

    @OneToMany(mappedBy = "formateur")
    @JsonIgnoreProperties({"formateur"})
    private List<Cours> coursDonnes;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnoreProperties({"etudiant"})
    private List<Note> notes;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnoreProperties({"etudiant"})
    private List<Presence> presences;

    // ✅ Student -> Groupe (ManyToOne)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "groupe_id")
    @JsonIgnoreProperties({"students", "trainers"})
    private Groupe studentGroupe;

    // ✅ Trainer -> Groupes (ManyToMany)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "trainer_groupes",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
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
        return "User{id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
