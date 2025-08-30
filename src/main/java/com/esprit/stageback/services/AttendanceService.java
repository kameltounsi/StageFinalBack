package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface AttendanceService {
    List<EmploiOptionDTO> todaySeancesForTrainer(Long trainerId, ZoneId zone);
    RosterViewDTO getRoster(Long emploiId, Long trainerId);
    RosterViewDTO mark(Long emploiId, Long trainerId, List<PresenceMarkInput> entries);

    // History / filtre
    List<EmploiOptionDTO> sessionsBetweenForTrainer(Long trainerId, LocalDate start, LocalDate end);

    // Choix classe → séance
    List<GroupOptionDTO> groupsForTrainer(Long trainerId);
    List<EmploiOptionDTO> sessionsByGroup(Long trainerId, Long groupId, LocalDate start, LocalDate end);
}
