// src/main/java/com/esprit/stageback/services/TrainerScheduleExportService.java
package com.esprit.stageback.services;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerScheduleExportService {

    private final UserRepository userRepository;
    private final EmploiTempsRepository edtRepository;
    private final PdfWeeklyScheduleRenderer pdf;

    public record WeekRange(LocalDate start, LocalDate end) {}

    /** Calcule Lundi→Dimanche pour n’importe quelle date donnée. */
    public WeekRange weekRange(LocalDate anyDate) {
        DayOfWeek dow = anyDate.getDayOfWeek();
        int shift = (dow.getValue() + 6) % 7; // Lundi=0..Dimanche=6
        LocalDate monday = anyDate.minusDays(shift);
        LocalDate sunday = monday.plusDays(6);
        return new WeekRange(monday, sunday);
    }

    public byte[] exportForTrainer(Long trainerId, LocalDate start, LocalDate ignoredEnd) {
        WeekRange r = weekRange(start);
        LocalDate monday = r.start();
        LocalDate sunday = r.end();

        // Récup infos formateur
        var userOpt = userRepository.findById(trainerId);
        String trainerName      = userOpt.map(u -> u.getFullName()).orElse("#" + trainerId);
        String trainerEmail     = userOpt.map(u -> u.getEmail()).orElse(null);
        String trainerSpecialty = userOpt.map(u -> u.getSpecialite()).orElse(null);

        List<EmploiTemps> items = edtRepository.findWeeklyForTrainer(trainerId, monday, sunday);
        items = items.stream().flatMap(e -> splitAroundLunch(e).stream()).toList();

        List<LocalDate> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) days.add(monday.plusDays(i));

        List<Slot> slots = List.of(
                new Slot(LocalTime.of(8, 0),  LocalTime.of(10, 0), "08:00 – 10:00"),
                new Slot(LocalTime.of(10, 0), LocalTime.of(12, 0), "10:00 – 12:00"),
                new Slot(LocalTime.of(13, 0), LocalTime.of(15, 0), "13:00 – 15:00"),
                new Slot(LocalTime.of(15, 0), LocalTime.of(17, 0), "15:00 – 17:00")
        );

        List<List<List<EmploiTemps>>> cellsPerDay = buildCells(days, slots, items);

        Map<String, Object> model = new HashMap<>();
        model.put("trainerName", trainerName);
        model.put("trainerEmail", trainerEmail);          // <-- ajouté
        model.put("trainerSpecialty", trainerSpecialty);  // <-- ajouté
        model.put("start", monday);
        model.put("end", sunday);
        model.put("days", days);
        model.put("slots", slots.stream().map(Slot::label).toList());
        model.put("cellsPerDay", cellsPerDay);
        model.put("now", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .format(LocalDateTime.now()));

        return pdf.renderTrainerWeekly(model);
    }

    // ---------- Helpers ----------

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END   = LocalTime.of(13, 0);

    private record Slot(LocalTime start, LocalTime end, String label) {}

    /** Découpe une séance qui traverse 12:00–13:00 en 2 parties; sinon renvoie tel quel. */
    private List<EmploiTemps> splitAroundLunch(EmploiTemps e) {
        LocalTime start = e.getHeureDebut();
        LocalTime end   = e.getHeureFin();

        // Entièrement avant ou après la pause → rien à faire
        if (end.compareTo(LUNCH_START) <= 0 || start.compareTo(LUNCH_END) >= 0) {
            return List.of(e);
        }

        List<EmploiTemps> parts = new ArrayList<>(2);

        // Partie du matin
        if (start.isBefore(LUNCH_START)) {
            parts.add(copyWithTimes(e, start, LUNCH_START));
        }
        // Partie de l’après-midi
        if (end.isAfter(LUNCH_END)) {
            parts.add(copyWithTimes(e, LUNCH_END, end));
        }
        return parts.isEmpty() ? List.of() : parts;
    }

    private EmploiTemps copyWithTimes(EmploiTemps src, LocalTime start, LocalTime end) {
        EmploiTemps c = new EmploiTemps();
        c.setId(src.getId()); // ou null si tu préfères un ID “virtuel” en export
        c.setDate(src.getDate());
        c.setMatiere(src.getMatiere());
        c.setSalle(src.getSalle());
        c.setGroupe(src.getGroupe());
        c.setFormateur(src.getFormateur());
        c.setHeureDebut(start);
        c.setHeureFin(end);
        return c;
    }

    private List<List<List<EmploiTemps>>> buildCells(
            List<LocalDate> days,
            List<Slot> slots,
            List<EmploiTemps> items
    ) {
        // init 7 × S
        List<List<List<EmploiTemps>>> cells = new ArrayList<>(7);
        for (int di = 0; di < 7; di++) {
            List<List<EmploiTemps>> row = new ArrayList<>(slots.size());
            for (int si = 0; si < slots.size(); si++) row.add(new ArrayList<>());
            cells.add(row);
        }

        // index de jour
        Map<LocalDate, Integer> dayIndex = new HashMap<>();
        for (int i = 0; i < days.size(); i++) dayIndex.put(days.get(i), i);

        // Dispatch par overlap
        for (EmploiTemps e : items) {
            Integer di = dayIndex.get(e.getDate());
            if (di == null) continue;

            for (int si = 0; si < slots.size(); si++) {
                Slot s = slots.get(si);
                if (overlaps(e.getHeureDebut(), e.getHeureFin(), s.start, s.end)) {
                    cells.get(di).get(si).add(e);
                }
            }
        }

        // Tri par heure de début dans chaque cellule
        for (var row : cells) {
            for (var cell : row) {
                cell.sort(Comparator.comparing(EmploiTemps::getHeureDebut));
            }
        }
        return cells;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }
}
