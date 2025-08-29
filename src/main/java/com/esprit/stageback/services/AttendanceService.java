// src/main/java/com/esprit/stageback/services/AttendanceService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.EmploiOptionDTO;
import com.esprit.stageback.dto.PresenceMarkInput;
import com.esprit.stageback.dto.RosterViewDTO;

import java.time.ZoneId;
import java.util.List;

public interface AttendanceService {

    /**
     * Liste des séances d'aujourd'hui (à venir ou en cours) pour un formateur.
     * Le filtrage se base sur EmploiTemps.date / heureDebut / heureFin.
     */
    List<EmploiOptionDTO> todaySeancesForTrainer(Long trainerId, ZoneId zoneId);

    /**
     * Version pratique avec timezone par défaut Africa/Tunis.
     */
    default List<EmploiOptionDTO> todaySeancesForTrainer(Long trainerId) {
        return todaySeancesForTrainer(trainerId, ZoneId.of("Africa/Tunis"));
    }

    /**
     * Récupère le roster (étudiants du groupe) + statut de présence courant pour une séance.
     * Vérifie que le trainerId est autorisé (formateur de la séance) ou ADMIN.
     */
    RosterViewDTO getRoster(Long emploiId, Long trainerId);

    /**
     * Enregistre en lot les statuts (PRESENT/ABSENT) pour la séance.
     * Idempotent : upsert par (etudiant, emploiTemps). Retourne la vue mise à jour.
     */
    RosterViewDTO mark(Long emploiId, Long trainerId, List<PresenceMarkInput> entries);
}
