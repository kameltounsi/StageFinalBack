package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.NoteClaimRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepo;
    private final GroupeRepository groupeRepo;
    private final NoteClaimRepository claimRepo;
    private final AdminResultsService adminResultsService; // reuse your preview calculations

    private static final Pattern A_RE = Pattern.compile("(?i)\\bA\\b");
    private static final Pattern B_RE = Pattern.compile("(?i)\\bB\\b");

    @Override
    public AdminOverviewDTO getOverview() {
        int totalStudents    = (int) userRepo.countByRole(Roles.STUDENT);
        int unassigned       = (int) userRepo.countByRoleAndStudentGroupeIsNull(Roles.STUDENT);
        int totalGroups      = (int) groupeRepo.count();
        int lowCapacity      = (int) groupeRepo.countByStudentCapacityLessThanEqual(3);

        // If you don’t track statuses, this is total claims:
        int pendingClaims    = (int) claimRepo.count();

        // Count groups with all grades complete (promotion-ready)
        int promoReady = 0;
        for (Groupe g : groupeRepo.findAll()) {
            try {
                AdminGroupResultsPreviewDTO prev = adminResultsService.previewGroupResults(g.getId());
                if (prev.isAllComplete()) promoReady++;
            } catch (Exception ignored) {
                // ignore non-gradeable groups
            }
        }

        var students = new AdminOverviewDTO.Students(totalStudents, unassigned, 0);
        var groups   = new AdminOverviewDTO.Groups(totalGroups, lowCapacity);
        var claims   = new AdminOverviewDTO.Claims(pendingClaims);

        return AdminOverviewDTO.builder()
                .students(students)
                .groups(groups)
                .claims(claims)
                .promotionReadyGroups(promoReady)
                .build();
    }

    @Override
    public List<GroupStatusRowDTO> getGroupStatus() {
        return groupeRepo.findAll().stream().map(g -> {
            int scount = (int) userRepo.countByStudentGroupe_Id(g.getId());
            int claims = (int) claimRepo.countByStudent_StudentGroupe_Id(g.getId());

            String level = null;
            String name  = g.getNom() != null ? g.getNom().trim() : "";
            if (A_RE.matcher(name).find()) level = "A";
            else if (B_RE.matcher(name).find()) level = "B";

            int admitted = 0, rejected = 0, incomplete = 0;
            boolean complete = false;
            try {
                AdminGroupResultsPreviewDTO prev = adminResultsService.previewGroupResults(g.getId());
                admitted   = (int) prev.getAdmittedCount();
                rejected   = (int) prev.getRefusedCount();
                incomplete = (int) prev.getIncompleteCount();
                complete   = prev.isAllComplete();
            } catch (Exception ignored) {}

            return GroupStatusRowDTO.builder()
                    .id(g.getId())
                    .name(g.getNom())
                    .specialite(g.getSpecialite())
                    .level(level)
                    .studentCount(scount)
                    .studentCapacityLeft(g.getStudentCapacity())
                    .trainerCapacityLeft(g.getTrainerCapacity())
                    .pendingClaims(claims)
                    .gradingComplete(complete)
                    .admittedCount(admitted)
                    .rejectedCount(rejected)
                    .incompleteCount(incomplete)
                    .build();
        }).toList();
    }

    public List<TrainersBySpecialiteDTO> getTrainersBySpecialite() {
        List<User> trainers = userRepo.findAllTrainersWithGroups();

        // Groupement par spécialité (valeur par défaut "Unspecified")
        Map<String, List<User>> bySpec = trainers.stream()
                .collect(Collectors.groupingBy(u -> {
                    String s = Optional.ofNullable(u.getSpecialite()).orElse("").trim();
                    return s.isEmpty() ? "Unspecified" : s;
                }, LinkedHashMap::new, Collectors.toList()));

        List<TrainersBySpecialiteDTO> out = new ArrayList<>();

        for (var entry : bySpec.entrySet()) {
            String spec = entry.getKey();
            List<User> list = entry.getValue();

            // tri par nom (optionnel)
            list.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));

            List<TrainerRowDTO> rows = list.stream()
                    .map(t -> TrainerRowDTO.builder()
                            .id(t.getId())
                            .fullName(t.getFullName())
                            .email(t.getEmail())
                            .profilePicture(t.getProfilePicture())
                            .groups(Optional.ofNullable(t.getTrainerGroupes()).orElseGet(List::of)
                                    .stream()
                                    .map(g -> GroupRefDTO.builder().id(g.getId()).name(g.getNom()).build())
                                    .toList())
                            .build())
                    .toList();

            out.add(TrainersBySpecialiteDTO.builder()
                    .specialite(spec)
                    .trainers(rows)
                    .build());
        }

        return out;
    }

    public List<SpecialiteCountDTO> getStudentsBySpecialite() {
        return userRepo.countStudentsBySpecialite();
    }
}
