package com.esprit.stageback.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TrainerScheduleItemDto(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String subject,
        String room,
        Long groupId,
        String groupName,
        String groupSpeciality
) {}
