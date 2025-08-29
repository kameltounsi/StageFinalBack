// src/main/java/com/esprit/stageback/services/impl/AttendanceServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.EmploiOptionDTO;
import com.esprit.stageback.dto.PresenceMarkInput;
import com.esprit.stageback.dto.RosterRowDTO;
import com.esprit.stageback.dto.RosterViewDTO;
import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Presence;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.PresenceRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final EmploiTempsRepository emploiRepo;
    private final PresenceRepository presenceRepo;
    private final UserRepository userRepo;

    // --------- helpers

    private void assertTrainerOwnsEmploi(User trainer, EmploiTemps e) {
        boolean isAdmin = trainer.getRole() == Roles.ADMIN;
        if (!isAdmin && (e.getFormateur() == null || !Objects.equals(e.getFormateur().getId(), trainer.getId()))) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à gérer cet emploi.");
        }
    }

    private List<User> groupStudents(EmploiTemps e) {
        return Optional.ofNullable(e.getGroupe())
                .map(Groupe::getStudents)
                .orElseGet(List::of);
    }

    // --------- interface impl

    @Override
    public List<EmploiOptionDTO> todaySeancesForTrainer(Long trainerId, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalTime now   = LocalTime.now(zone);

        return emploiRepo.findTodayUpcomingOrOngoingForTrainer(trainerId, today, now)
                .stream()
                .map(e -> EmploiOptionDTO.builder()
                        .id(e.getId())
                        .date(e.getDate())
                        .start(e.getHeureDebut())
                        .end(e.getHeureFin())
                        .groupeId(e.getGroupe() != null ? e.getGroupe().getId() : null)
                        .groupeNom(e.getGroupe() != null ? e.getGroupe().getNom() : null)
                        .matiere(e.getMatiere())
                        .salle(e.getSalle())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RosterViewDTO getRoster(Long emploiId, Long trainerId) {
        EmploiTemps e = emploiRepo.findById(emploiId)
                .orElseThrow(() -> new NoSuchElementException("Séance introuvable: " + emploiId));
        User trainer = userRepo.findById(trainerId).orElseThrow();
        assertTrainerOwnsEmploi(trainer, e);

        List<User> students = groupStudents(e);

        Map<Long, Presence> presenceByStudent = presenceRepo.findByEmploiTemps_Id(e.getId())
                .stream()
                .collect(Collectors.toMap(p -> p.getEtudiant().getId(), p -> p));

        List<RosterRowDTO> rows = students.stream()
                .map(s -> RosterRowDTO.builder()
                        .studentId(s.getId())
                        .fullName(s.getFullName())
                        .current(Optional.ofNullable(presenceByStudent.get(s.getId()))
                                .map(Presence::getStatut)
                                .orElse(null))
                        .build())
                .sorted(Comparator.comparing(RosterRowDTO::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return RosterViewDTO.builder()
                .emploiId(e.getId())
                .date(e.getDate())
                .start(e.getHeureDebut())
                .end(e.getHeureFin())
                .groupeId(e.getGroupe() != null ? e.getGroupe().getId() : null)
                .groupeNom(e.getGroupe() != null ? e.getGroupe().getNom() : null)
                .matiere(e.getMatiere())
                .rows(rows)
                .build();
    }

    @Override
    public RosterViewDTO mark(Long emploiId, Long trainerId, List<PresenceMarkInput> entries) {
        EmploiTemps e = emploiRepo.findById(emploiId)
                .orElseThrow(() -> new NoSuchElementException("Séance introuvable: " + emploiId));
        User trainer = userRepo.findById(trainerId).orElseThrow();
        assertTrainerOwnsEmploi(trainer, e);

        // Vérifier appartenance des étudiants au groupe
        Set<Long> groupStudentIds = groupStudents(e).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        for (PresenceMarkInput in : entries) {
            if (in.getStudentId() == null) {
                throw new IllegalArgumentException("studentId manquant.");
            }
            if (in.getStatut() == null) {
                throw new IllegalArgumentException("Statut requis (PRESENT ou ABSENT).");
            }
            if (!groupStudentIds.contains(in.getStudentId())) {
                throw new IllegalArgumentException("L'étudiant " + in.getStudentId() + " n'appartient pas au groupe.");
            }
        }

        // Upsert: un seul enregistrement par (étudiant, emploi)
        for (PresenceMarkInput in : entries) {
            Presence p = presenceRepo.findByEtudiant_IdAndEmploiTemps_Id(in.getStudentId(), e.getId())
                    .orElse(Presence.builder()
                            .etudiant(User.builder().id(in.getStudentId()).build())
                            .emploiTemps(e)
                            .build());

            p.setStatut(in.getStatut()); // exclusivité: on stocke 1 statut
            presenceRepo.save(p);
        }

        // Retourner la vue mise à jour
        return getRoster(emploiId, trainerId);
    }
}
