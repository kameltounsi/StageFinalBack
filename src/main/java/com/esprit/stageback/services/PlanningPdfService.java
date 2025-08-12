package com.esprit.stageback.services;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource; // <-- AJOUT
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningPdfService {

    private final EmploiTempsRepository emploiTempsRepository;
    private final GroupeRepository groupeRepository;
    private final SpringTemplateEngine templateEngine;

    private record Slot(LocalTime start, LocalTime end, String label) {}

    private static final List<Slot> SLOTS = List.of(
            new Slot(LocalTime.of(8, 0),  LocalTime.of(10, 0), "08:00 – 10:00"),
            new Slot(LocalTime.of(10, 0), LocalTime.of(12, 0), "10:00 – 12:00"),
            // 12–13 : PAUSE
            new Slot(LocalTime.of(13, 0), LocalTime.of(15, 0), "13:00 – 15:00"),
            new Slot(LocalTime.of(15, 0), LocalTime.of(17, 0), "15:00 – 17:00")
    );

    private static final DateTimeFormatter DAY_HEADER_FMT =
            DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);
    private static final DateTimeFormatter WEEK_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public byte[] buildPlanningPdf(Long groupeId, LocalDate start, LocalDate end) {
        Groupe g = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new NoSuchElementException("Groupe introuvable"));

        List<EmploiTemps> items = emploiTempsRepository.findPlanning(groupeId, start, end);

        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        List<LocalDate> dayList = new ArrayList<>(days);
        for (int i = 0; i < days; i++) dayList.add(start.plusDays(i));

        Map<LocalDate, List<EmploiTemps>> byDay = items.stream()
                .collect(Collectors.groupingBy(EmploiTemps::getDate));
        byDay.values().forEach(list ->
                list.sort(Comparator.comparing(EmploiTemps::getHeureDebut, Comparator.nullsLast(Comparator.naturalOrder())))
        );

        List<List<List<EmploiTemps>>> cellsPerDay = new ArrayList<>();
        for (LocalDate d : dayList) {
            List<EmploiTemps> daySessions = byDay.getOrDefault(d, Collections.emptyList());
            List<List<EmploiTemps>> row = new ArrayList<>();
            for (Slot s : SLOTS) {
                List<EmploiTemps> inSlot = daySessions.stream()
                        .filter(e -> overlaps(e.getHeureDebut(), e.getHeureFin(), s.start(), s.end()))
                        .toList();
                row.add(inSlot);
            }
            cellsPerDay.add(row);
        }

        String weekLabel = "Semaine du " + WEEK_FMT.format(start) + " au " + WEEK_FMT.format(end);
        List<String> dayHeaders = dayList.stream().map(d -> capitalize(DAY_HEADER_FMT.format(d))).toList();
        List<String> slotLabels = SLOTS.stream().map(Slot::label).toList();

        Context ctx = new Context(Locale.FRENCH);
        ctx.setVariable("groupe", g);
        ctx.setVariable("groupName", g.getNom());
        ctx.setVariable("weekLabel", weekLabel);
        ctx.setVariable("days", dayList);
        ctx.setVariable("dayHeaders", dayHeaders); // (ok si non utilisé par le template)
        ctx.setVariable("slots", slotLabels);      // <-- AJOUT : pour que le template puisse les utiliser
        ctx.setVariable("cellsPerDay", cellsPerDay);
        ctx.setVariable("start", start);           // <-- AJOUT
        ctx.setVariable("end", end);               // <-- AJOUT
        ctx.setVariable("now", java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String html = templateEngine.process("planning-semaine", ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // <-- AJOUT : baseUri pour que /images/logo.png (dans static/) soit trouvé
            String baseUri = new ClassPathResource("static/").getURL().toString();
            builder.withHtmlContent(html, baseUri);

            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération PDF", e);
        }
    }

    private static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        if (aStart == null || aEnd == null) return false;
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase(Locale.FRENCH) + s.substring(1);
    }
}
