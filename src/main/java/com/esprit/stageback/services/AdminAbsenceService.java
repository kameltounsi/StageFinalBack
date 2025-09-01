package com.esprit.stageback.services;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.dto.AdminAbsenceSummaryRow;
import com.esprit.stageback.entities.Groupe;

import java.time.LocalDate;
import java.util.List;

public interface AdminAbsenceService {
    List<String> listSpecialites();
    List<Groupe> groupsBySpecialite(String specialite);

    List<AdminAbsenceSummaryRow> summary(
            String specialite, Long groupId, LocalDate start, LocalDate end, String sortBy, String dir);

    List<AdminAbsenceDetailItem> detailsForStudent(
            Long studentId, String specialite, Long groupId, LocalDate start, LocalDate end);

    // NEW:
    void sendAlertToStudent(Long studentId, long minUnjustified);
    int sendBulkAlerts(String specialite, Long groupId, LocalDate start, LocalDate end, long minUnjustified);
}
