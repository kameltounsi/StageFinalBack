package com.esprit.stageback.services;

import com.esprit.stageback.entities.EmploiTemps;
import java.time.LocalDate;
import java.util.List;

public interface EmploiTempsService {
    EmploiTemps ajouterEmploi(Long groupeId, EmploiTemps emploiTemps);
    List<EmploiTemps> getPlanningByGroupe(Long groupeId);
    List<EmploiTemps> getPlanningBetweenDates(LocalDate start, LocalDate end);
    void supprimerEmploi(Long emploiId);
}
