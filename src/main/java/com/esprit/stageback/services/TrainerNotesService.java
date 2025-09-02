// src/main/java/com/esprit/stageback/services/TrainerNotesService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.GradeRow;
import com.esprit.stageback.dto.SaveGradesRequest;

import java.util.List;

public interface TrainerNotesService {
    List<GradeRow> loadSheet(String trainerEmail, Long groupeId, String matiere);
    void saveBulk(String trainerEmail, SaveGradesRequest payload);
}
