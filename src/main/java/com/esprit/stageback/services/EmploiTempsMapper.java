package com.esprit.stageback.services;

import com.esprit.stageback.dto.EmploiTempsDTO;
import com.esprit.stageback.entities.EmploiTemps;

public class EmploiTempsMapper {
    public static EmploiTempsDTO toDTO(EmploiTemps emploi) {
        EmploiTempsDTO dto = new EmploiTempsDTO();
        dto.setId(emploi.getId());
        dto.setDate(emploi.getDate());
        dto.setHeureDebut(emploi.getHeureDebut());
        dto.setHeureFin(emploi.getHeureFin());
        dto.setSalle(emploi.getSalle());
        dto.setMatiere(emploi.getMatiere());
        dto.setFormateurNom(
                emploi.getFormateur() != null ? emploi.getFormateur().getFullName() : null
        );
        dto.setGroupeNom(
                emploi.getGroupe() != null ? emploi.getGroupe().getNom() : null
        );
        return dto;
    }
}
