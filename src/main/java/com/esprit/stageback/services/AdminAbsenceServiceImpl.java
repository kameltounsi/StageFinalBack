// src/main/java/com/esprit/stageback/services/impl/AdminAbsenceServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.dto.AdminAbsenceSummaryRow;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.PresenceRepository;
import com.esprit.stageback.services.AdminAbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAbsenceServiceImpl implements AdminAbsenceService {

    private final GroupeRepository groupeRepo;
    private final PresenceRepository presenceRepo;

    @Override
    public List<String> listSpecialites() {
        return groupeRepo.findDistinctSpecialites();
    }

    @Override
    public List<Groupe> groupsBySpecialite(String specialite) {
        return groupeRepo.findBySpecialiteIgnoreCase(specialite);
    }

    @Override
    public List<AdminAbsenceSummaryRow> summary(
            String specialite, Long groupId, LocalDate start, LocalDate end, String sortBy, String dir) {

        var rows = presenceRepo.aggregateAbsencesByStudent(specialite, groupId, start, end)
                .stream()
                .map(a -> AdminAbsenceSummaryRow.builder()
                        .studentId((Long) a[0])
                        .studentName((String) a[1])
                        .studentEmail((String) a[2])
                        .groupId((Long) a[3])
                        .groupName((String) a[4])
                        .specialite((String) a[5])
                        .totalAbsences(((Number) a[6]).longValue())
                        .unjustifiedAbsences(((Number) a[7]).longValue())
                        .build()
                ).toList();

        Comparator<AdminAbsenceSummaryRow> cmp =
                switch (sortBy == null ? "" : sortBy) {
                    case "name" -> Comparator.comparing(AdminAbsenceSummaryRow::getStudentName, String.CASE_INSENSITIVE_ORDER);
                    case "unjustified" -> Comparator.comparingLong(AdminAbsenceSummaryRow::getUnjustifiedAbsences);
                    default -> Comparator.comparingLong(AdminAbsenceSummaryRow::getTotalAbsences);
                };

        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return rows.stream().sorted(cmp).toList();
    }

    @Override
    public List<AdminAbsenceDetailItem> detailsForStudent(
            Long studentId, String specialite, Long groupId, LocalDate start, LocalDate end) {
        return presenceRepo.findAbsenceDetailsForStudent(studentId, specialite, groupId, start, end)
                .stream().map(r -> AdminAbsenceDetailItem.builder()
                        .presenceId((Long) r[0])
                        .date((java.time.LocalDate) r[1])
                        .start((java.time.LocalTime) r[2])
                        .end((java.time.LocalTime) r[3])
                        .matiere((String) r[4])
                        .room((String) r[5])
                        .statut((com.esprit.stageback.entities.StatutPresence) r[6])
                        .build()
                ).toList();
    }
}
