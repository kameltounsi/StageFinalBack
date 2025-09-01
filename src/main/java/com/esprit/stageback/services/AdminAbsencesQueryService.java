// src/main/java/com/esprit/stageback/services/AdminAbsencesQueryService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.dto.AdminAbsenceSummaryRow;
import com.esprit.stageback.entities.Groupe;

import java.time.LocalDate;
import java.util.List;

public interface AdminAbsencesQueryService {

    // --- Filtres de base ---
    List<String> specialites();

    List<Groupe> groupsBySpecialite(String specialite);

    List<AdminAbsenceSummaryRow> listSummary(
            String specialite, Long groupId, LocalDate start, LocalDate end,
            String sortBy, String dir
    );

    List<AdminAbsenceDetailItem> listDetailsForStudent(
            Long studentId, String specialite, Long groupId, LocalDate start, LocalDate end
    );

    int getUnjustifiedCountForStudent(Long studentId, LocalDate start, LocalDate end);
}
