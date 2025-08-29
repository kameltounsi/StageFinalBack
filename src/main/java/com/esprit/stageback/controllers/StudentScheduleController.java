// src/main/java/com/esprit/stageback/controllers/StudentScheduleController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.StudentWeeklyItemDto;
import com.esprit.stageback.services.StudentScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/studentsSchedule")
@RequiredArgsConstructor
public class StudentScheduleController {

    private final StudentScheduleService service;

    // For the authenticated student
    // GET /api/students/me/weekly-schedule?start=YYYY-MM-DD&end=YYYY-MM-DD
    @GetMapping("/me/weekly-schedule")
    public List<StudentWeeklyItemDto> myWeeklySchedule(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getForCurrentStudent(start, end);
    }

    // Optional: explore by group (admin)
    // GET /api/students/{groupId}/weekly-schedule?start=YYYY-MM-DD&end=YYYY-MM-DD
    @GetMapping("/{groupId}/weekly-schedule")
    public List<StudentWeeklyItemDto> groupWeeklySchedule(
            @PathVariable Long groupId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getForGroup(groupId, start, end);
    }
}
