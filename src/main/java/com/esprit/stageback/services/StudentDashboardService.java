// src/main/java/com/esprit/stageback/services/StudentDashboardService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.DaySessionDTO;
import com.esprit.stageback.dto.GradeRowDTO;
import com.esprit.stageback.dto.StudentOverviewDTO;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final UserRepository userRepository;

    public StudentOverviewDTO overview(Principal principal) {
        String email = principal.getName();
        Optional<User> opt = userRepository.findByEmail(email);
        User me = opt.orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // Récupération de l’id du groupe via les méthodes existantes
        Optional<Long> grpIdOpt = userRepository.findStudentGroupIdByUserId(me.getId());

        return StudentOverviewDTO.builder()
                .studentName(me.getFullName())
                .groupId(grpIdOpt.orElse(null))
                .groupName(me.getStudentGroupe() != null ? me.getStudentGroupe().getNom() : null)
                .specialite(me.getSpecialite())

                // placeholders (à brancher plus tard sur tes vraies sources)
                .average(null)
                .attendedSessions(0)
                .missedSessions(0)
                .pendingClaims(0)
                .unreadMessages(0)

                // exemple de prochain cours (null par défaut)
                .nextSession(null)
                .build();
    }

    public List<DaySessionDTO> today(Principal principal) {
        // TODO : brancher sur plannings (pour l’instant, vide = aucun cours aujourd’hui)
        return Collections.emptyList();
    }

    public List<GradeRowDTO> grades(Principal principal) {
        // TODO : brancher sur notes (pour l’instant, vide)
        return Collections.emptyList();
    }
}
