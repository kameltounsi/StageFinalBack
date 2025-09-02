package com.esprit.stageback.services;

import com.esprit.stageback.dto.GroupSummaryDTO;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerGroupsService {
    private final UserRepository userRepository;
    private final GroupeRepository groupeRepository;

    public List<GroupSummaryDTO> myGroups(String trainerEmail) {
        var ids = userRepository.findTrainerGroupIdsByEmailIgnoreCase(trainerEmail);
        if (ids == null || ids.isEmpty()) return List.of();
        List<Groupe> groups = groupeRepository.findByIdInOrderByNomAsc(ids);
        return groups.stream()
                .map(g -> new GroupSummaryDTO(g.getId(), g.getNom(), g.getSpecialite()))
                .toList();
    }
}
