package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.*;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.PresenceRepository;
import com.esprit.stageback.repositories.UserRepository;
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
    private final GroupeRepository groupeRepo;

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

    // -------- Today (raccourci du jour) --------
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

    // -------- Roster (lecture) --------
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
                        .email(s.getEmail()) // 👈 email renvoyé au front
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

    // -------- Mark (écriture) --------
    @Override
    public RosterViewDTO mark(Long emploiId, Long trainerId, List<PresenceMarkInput> entries) {
        EmploiTemps e = emploiRepo.findById(emploiId)
                .orElseThrow(() -> new NoSuchElementException("Séance introuvable: " + emploiId));
        User trainer = userRepo.findById(trainerId).orElseThrow();
        assertTrainerOwnsEmploi(trainer, e);

        Set<Long> groupStudentIds = groupStudents(e).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        for (PresenceMarkInput in : entries) {
            if (in.getStudentId() == null) throw new IllegalArgumentException("studentId manquant.");
            if (in.getStatut() == null)     throw new IllegalArgumentException("Statut requis.");
            if (!groupStudentIds.contains(in.getStudentId())) {
                throw new IllegalArgumentException("L'étudiant " + in.getStudentId() + " n'appartient pas au groupe.");
            }
        }

        for (PresenceMarkInput in : entries) {
            Presence p = presenceRepo.findByEtudiant_IdAndEmploiTemps_Id(in.getStudentId(), e.getId())
                    .orElse(Presence.builder()
                            .etudiant(User.builder().id(in.getStudentId()).build())
                            .emploiTemps(e)
                            .build());
            p.setStatut(in.getStatut());
            presenceRepo.save(p);
        }

        return getRoster(emploiId, trainerId);
    }

    // -------- History (intervalle) --------
    @Override
    @Transactional(readOnly = true)
    public List<EmploiOptionDTO> sessionsBetweenForTrainer(Long trainerId, LocalDate start, LocalDate end) {
        return emploiRepo.findByFormateurIdAndDateBetweenOrderByDateAscHeureDebutAsc(trainerId, start, end)
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

    // -------- Classe → séance --------
    @Override
    @Transactional(readOnly = true)
    public List<GroupOptionDTO> groupsForTrainer(Long trainerId) {
        return groupeRepo.findByTrainers_Id(trainerId)
                .stream()
                .map(g -> GroupOptionDTO.builder()
                        .id(g.getId())
                        .nom(g.getNom())
                        .specialite(g.getSpecialite())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmploiOptionDTO> sessionsByGroup(Long trainerId, Long groupId, LocalDate start, LocalDate end) {
        // sécurise : ne lister que si le prof appartient au groupe (ou ADMIN)
        User trainer = userRepo.findById(trainerId).orElseThrow();
        boolean owns = groupeRepo.findByTrainers_Id(trainerId).stream().anyMatch(g -> Objects.equals(g.getId(), groupId));
        if (!owns && trainer.getRole() != Roles.ADMIN) {
            throw new AccessDeniedException("Non autorisé pour ce groupe.");
        }
        LocalDate s = Optional.ofNullable(start).orElse(LocalDate.now().minusMonths(1));
        LocalDate e = Optional.ofNullable(end).orElse(LocalDate.now().plusMonths(1));

        return emploiRepo.findWeeklyForGroup(groupId, s, e)  // trié par date + heure
                .stream()
                .map(et -> EmploiOptionDTO.builder()
                        .id(et.getId())
                        .date(et.getDate())
                        .start(et.getHeureDebut())
                        .end(et.getHeureFin())
                        .groupeId(groupId)
                        .groupeNom(et.getGroupe() != null ? et.getGroupe().getNom() : null)
                        .matiere(et.getMatiere())
                        .salle(et.getSalle())
                        .build())
                .toList();
    }
}
