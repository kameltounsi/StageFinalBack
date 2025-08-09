package com.esprit.stageback.services;

import com.esprit.stageback.dto.EmploiTempsDTO;
import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class EmploiTempsServiceImpl implements EmploiTempsService {

    private final EmploiTempsRepository emploiTempsRepository;
    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;
/*
    @Override
    public EmploiTemps ajouterEmploi(Long groupeId, EmploiTemps emploiTemps) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        emploiTemps.setGroupe(groupe);

        // ⚡ On suppose que le formateur est déjà attaché dans le body
        return emploiTempsRepository.save(emploiTemps);
    }*/
@Override
public EmploiTemps ajouterEmploi(Long groupeId, EmploiTemps emploiTemps) {
    Groupe groupe = groupeRepository.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

    // ⚠️ Attacher le formateur depuis la DB si un id est fourni
    if (emploiTemps.getFormateur() != null && emploiTemps.getFormateur().getId() != null) {
        var formateur = userRepository.findById(emploiTemps.getFormateur().getId())
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        emploiTemps.setFormateur(formateur);
    } else {
        throw new RuntimeException("Formateur manquant");
    }

    emploiTemps.setGroupe(groupe);
    return emploiTempsRepository.save(emploiTemps);
}

    @Override
    public List<EmploiTemps> getPlanningByGroupe(Long groupeId) {
        return emploiTempsRepository.findByGroupeId(groupeId);
    }

    @Override
    public List<EmploiTemps> getPlanningBetweenDates(LocalDate startDate, LocalDate endDate) {
        return emploiTempsRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public void supprimerEmploi(Long emploiId) {
        emploiTempsRepository.deleteById(emploiId);
    }

}
