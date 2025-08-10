package com.esprit.stageback.services;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class EmploiTempsServiceImpl implements EmploiTempsService {

    private final EmploiTempsRepository emploiTempsRepository;
    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;
    @Transactional
    @Override
    public EmploiTemps ajouterEmploi(Long groupeId, EmploiTemps emploiTemps) {

        // ---------- Vérifs payload ----------
        if (emploiTemps == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requête vide");
        }
        if (emploiTemps.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date manquante");
        }
        if (emploiTemps.getHeureDebut() == null || emploiTemps.getHeureFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Heures manquantes");
        }
        if (emploiTemps.getSalle() == null || emploiTemps.getSalle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salle manquante");
        }
        if (emploiTemps.getMatiere() == null || emploiTemps.getMatiere().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Matière manquante");
        }
        if (emploiTemps.getFormateur() == null || emploiTemps.getFormateur().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formateur manquant");
        }

        // ---------- Récup entités ----------
        final Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe introuvable"));

        final var formateur = userRepository.findById(emploiTemps.getFormateur().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formateur introuvable"));

        // ---------- Règles métier : date/heure ----------
        final LocalDate today = LocalDate.now();
        if (emploiTemps.getDate().isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date ne peut pas être dans le passé");
        }

        final LocalTime start = emploiTemps.getHeureDebut();
        final LocalTime end   = emploiTemps.getHeureFin();
        final LocalTime BUSINESS_START = LocalTime.of(8, 0);
        final LocalTime BUSINESS_END   = LocalTime.of(17, 0);

        // autorisé dans [08:00, 17:00] et fin > début
        if (start.isBefore(BUSINESS_START) || end.isAfter(BUSINESS_END) || !end.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Horaires invalides — autorisés uniquement entre 08:00 et 17:00 (fin strictement après début)"
            );
        }

        // ---------- Normalisations ----------
        final String salle = emploiTemps.getSalle().trim();
        final String matiere = emploiTemps.getMatiere().trim();

        // ---------- Conflits (même jour) : salle / formateur ----------
        // NB: on considère chevauchement si (start < existFin) && (end > existDebut)
        // Vos requêtes existsSalleOverlap / existsFormateurOverlap doivent suivre cette logique.
        final boolean salleOccupee = emploiTempsRepository.existsSalleOverlap(
                emploiTemps.getDate(), salle, start, end
        );
        if (salleOccupee) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Salle déjà réservée sur cet intervalle");
        }

        final boolean formateurOccupe = emploiTempsRepository.existsFormateurOverlap(
                emploiTemps.getDate(), formateur.getId(), start, end
        );
        if (formateurOccupe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Formateur déjà occupé sur cet intervalle");
        }

        // ---------- Persistance ----------
        emploiTemps.setSalle(salle);
        emploiTemps.setMatiere(matiere);
        emploiTemps.setGroupe(groupe);
        emploiTemps.setFormateur(formateur);

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
