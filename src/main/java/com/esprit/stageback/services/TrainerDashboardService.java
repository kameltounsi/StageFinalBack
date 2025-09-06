// src/main/java/com/esprit/stageback/services/TrainerDashboardService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerDashboardService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    private String currentEmail() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a != null) ? a.getName() : null;
    }

    public TrainerOverviewDTO overview() {
        String email = currentEmail();
        if (email == null) {
            return TrainerOverviewDTO.builder().myGroups(0).myStudents(0).sessionsThisWeek(0)
                    .pendingClaims(0).ungradedSubmissions(0).build();
        }

        List<Long> myGroupIds = userRepository.findTrainerGroupIdsByEmailIgnoreCase(email);
        long totalStudents = myGroupIds.stream()
                .mapToLong(userRepository::countByStudentGroupe_Id)
                .sum();

        // If you don't track sessions/claims/submissions yet, keep zeros.
        return TrainerOverviewDTO.builder()
                .myGroups(myGroupIds.size())
                .myStudents(totalStudents)
                .sessionsThisWeek(0)
                .pendingClaims(0)
                .ungradedSubmissions(0)
                .build();
    }

    public List<TrainerGroupRowDTO> myGroups() {
        String email = currentEmail();
        List<Groupe> groups = groupeRepository.findGroupsForTrainerEmail(email);
        return groups.stream().map(g ->
                TrainerGroupRowDTO.builder()
                        .id(g.getId())
                        .name(g.getNom())
                        .specialite(g.getSpecialite())
                        .studentCount(userRepository.countByStudentGroupe_Id(g.getId()))
                        .build()
        ).collect(Collectors.toList());
    }

    public List<StudentMiniDTO> studentsOfGroup(Long groupId) {
        List<User> students = userRepository.findStudentsByGroupeId(groupId);
        return students.stream().map(u ->
                StudentMiniDTO.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .profilePicture(u.getProfilePicture())
                        .build()
        ).collect(Collectors.toList());
    }

    // Optional: weekly schedule – return empty list for now
    public List<ScheduleItemDTO> weeklySchedule(LocalDate weekStart) {
        // If you have a Planning repository, replace this stub with real data.
        return List.of();
    }
}