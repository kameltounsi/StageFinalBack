package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.TrainerScheduleExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trainersschedule")
@RequiredArgsConstructor
public class TrainerScheduleExportController {

    private final TrainerScheduleExportService exportService;
    private final UserRepository userRepository;

    /** Export PDF pour l’utilisateur connecté, avec start/end OU weekDate */
    @GetMapping(value = "/me/weekly-schedule.pdf", produces = "application/pdf")
    public void myWeeklyPdf(
            HttpServletResponse response,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false, name = "weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }

        // JwtAuthFilter met User comme principal (ou l’email en name)
        Long trainerId;
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            trainerId = u.getId();
        } else {
            String email = auth.getName();
            trainerId = userRepository.findIdByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        }

        // Déterminer la semaine
        LocalDate s;
        LocalDate e;
        if (start != null && end != null) {
            s = start; e = end;
        } else {
            LocalDate ref = (weekDate != null ? weekDate : LocalDate.now());
            var range = exportService.weekRange(ref);
            s = range.start(); e = range.end();
        }

        byte[] pdf = exportService.exportForTrainer(trainerId, s, e);
        String filename = "weekly_schedule_" + s + "_to_" + e + ".pdf";
        writePdf(response, filename, pdf);
    }

    // (Optionnel) Export pour un formateur donné (admin)
    @GetMapping(value = "/{trainerId}/weekly-schedule.pdf", produces = "application/pdf")
    public void trainerWeeklyPdf(
            HttpServletResponse response,
            @PathVariable Long trainerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) throws IOException {
        byte[] pdf = exportService.exportForTrainer(trainerId, start, end);
        String filename = "weekly_schedule_" + trainerId + "_" + start + "_to_" + end + ".pdf";
        writePdf(response, filename, pdf);
    }

    private void writePdf(HttpServletResponse res, String filename, byte[] bytes) throws IOException {
        res.setStatus(200);
        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        res.setContentLength(bytes.length);
        res.getOutputStream().write(bytes);
        res.flushBuffer();
    }
}
