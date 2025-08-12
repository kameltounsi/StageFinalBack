package com.esprit.stageback.services;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.GroupeRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlanningPdfService {

    private static final DateTimeFormatter KEY_FMT = DateTimeFormatter.ISO_DATE; // yyyy-MM-dd

    private final EmploiTempsRepository emploiTempsRepository;
    private final GroupeRepository groupeRepository;
    private final SpringTemplateEngine templateEngine;

    public byte[] buildPlanningPdf(Long groupeId, LocalDate start, LocalDate end) {
        Groupe g = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new NoSuchElementException("Groupe introuvable"));

        List<EmploiTemps> items = emploiTempsRepository.findPlanning(groupeId, start, end);

        // groupage par String yyyy-MM-dd
        Map<String, List<EmploiTemps>> byDay = items.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().format(KEY_FMT),
                        TreeMap::new,
                        Collectors.toList()
                ));
        byDay.replaceAll((k, v) -> {
            v.sort(Comparator.comparing(EmploiTemps::getHeureDebut,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            return v;
        });

        // liste des jours de start à end (inclus)
        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        List<LocalDate> dayList = IntStream.range(0, days)
                .mapToObj(start::plusDays)
                .toList();

        // >>> clé : fabriquer une liste alignée jour -> séances
        List<List<EmploiTemps>> sessionsPerDay = dayList.stream()
                .map(d -> byDay.getOrDefault(d.format(KEY_FMT), Collections.emptyList()))
                .toList();

        // Contexte
        Context ctx = new Context(Locale.FRENCH);
        ctx.setVariable("groupe", g);
        ctx.setVariable("start", start);
        ctx.setVariable("end", end);
        ctx.setVariable("days", dayList);
        ctx.setVariable("sessionsPerDay", sessionsPerDay); // <- utilisé par le template
        ctx.setVariable("now", java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String html = templateEngine.process("planning-semaine", ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération PDF", e);
        }
    }
}

