package com.esprit.stageback.services;

import com.esprit.stageback.dto.GradeRow;
import com.esprit.stageback.dto.SaveGradesRequest;

import java.time.LocalDate;
import java.util.List;

public interface TrainerNotesService {
    List<GradeRow> loadSheet(String trainerEmail, Long groupeId, LocalDate date, String matiere);
    void saveBulk(String trainerEmail, SaveGradesRequest payload);
}
