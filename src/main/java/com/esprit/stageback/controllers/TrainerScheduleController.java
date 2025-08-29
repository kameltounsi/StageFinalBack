// src/main/java/com/esprit/stageback/controllers/TrainerScheduleController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.WeeklyItemDto;
import com.esprit.stageback.services.TrainerScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerScheduleController {

    private final TrainerScheduleService service;

    // Pour l'utilisateur connecté (front : /api/trainers/me/weekly-schedule?start=YYYY-MM-DD&end=YYYY-MM-DD)
    @GetMapping("/me/weekly-schedule")
    public List<WeeklyItemDto> myWeeklySchedule(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getForCurrentTrainer(start, end);
    }

    // (Optionnel) Pour consulter le planning d’un formateur précis (utile admin)
    @GetMapping("/{trainerId}/weekly-schedule")
    public List<WeeklyItemDto> weeklyScheduleById(
            @PathVariable Long trainerId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getForTrainer(trainerId, start, end);
    }
}
