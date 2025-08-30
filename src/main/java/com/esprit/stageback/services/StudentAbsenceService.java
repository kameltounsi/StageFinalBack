// src/main/java/com/esprit/stageback/services/StudentAbsenceService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.StudentAbsenceSummaryDTO;

import java.time.LocalDate;

public interface StudentAbsenceService {
    StudentAbsenceSummaryDTO myAbsences(Long studentId, LocalDate start, LocalDate end);
}
