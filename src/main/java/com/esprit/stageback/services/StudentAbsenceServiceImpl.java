package com.esprit.stageback.services;
// src/main/java/com/esprit/stageback/services/impl/StudentAbsenceServiceImpl.java
import com.esprit.stageback.dto.StudentAbsenceItemDTO;
import com.esprit.stageback.dto.StudentAbsenceSummaryDTO;
import com.esprit.stageback.entities.Presence;
import com.esprit.stageback.entities.StatutPresence;
import com.esprit.stageback.repositories.PresenceRepository;
import com.esprit.stageback.services.StudentAbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAbsenceServiceImpl implements StudentAbsenceService {

    private final PresenceRepository presenceRepo;

    @Override
    public StudentAbsenceSummaryDTO myAbsences(Long studentId, LocalDate start, LocalDate end) {
        List<Presence> list = presenceRepo.findStudentAbsencesBetween(
                studentId, StatutPresence.ABSENT, start, end);

        long total = presenceRepo.countStudentAbsencesBetween(studentId, StatutPresence.ABSENT, start, end);
        long justified = presenceRepo.countStudentJustifiedBetween(studentId, StatutPresence.ABSENT, start, end);
        long unJustified = total - justified;

        List<StudentAbsenceItemDTO> items = list.stream().map(p -> {
            var e = p.getEmploiTemps();
            return StudentAbsenceItemDTO.builder()
                    .presenceId(p.getId())
                    .date(e.getDate())
                    .start(e.getHeureDebut())
                    .end(e.getHeureFin())
                    .matiere(e.getMatiere())
                    .groupeNom(e.getGroupe() != null ? e.getGroupe().getNom() : null)
                    .salle(e.getSalle())
                    .justified(p.getJustified())
                    .justificationNote(p.getJustificationNote())
                    .build();
        }).toList();

        return StudentAbsenceSummaryDTO.builder()
                .total(total)
                .justified(justified)
                .unJustified(unJustified)
                .items(items)
                .build();
    }
}
