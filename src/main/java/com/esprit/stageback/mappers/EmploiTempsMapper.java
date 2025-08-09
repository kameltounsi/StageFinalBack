// src/main/java/com/esprit/stageback/mappers/EmploiTempsMapper.java
package com.esprit.stageback.mappers;

import com.esprit.stageback.dto.EmploiTempsDTO;
import com.esprit.stageback.entities.EmploiTemps;

public class EmploiTempsMapper {
    public static EmploiTempsDTO toDTO(EmploiTemps e) {
        EmploiTempsDTO dto = new EmploiTempsDTO();
        dto.setId(e.getId());
        dto.setDate(e.getDate());
        dto.setHeureDebut(e.getHeureDebut());
        dto.setHeureFin(e.getHeureFin());
        dto.setSalle(e.getSalle());
        dto.setMatiere(e.getMatiere());
        dto.setFormateurNom(e.getFormateur() != null ? e.getFormateur().getFullName() : null);
        dto.setGroupeNom(e.getGroupe() != null ? e.getGroupe().getNom() : null);
        return dto;
    }
}
