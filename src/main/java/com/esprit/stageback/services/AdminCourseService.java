// src/main/java/com/esprit/stageback/services/AdminCourseService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.GroupDTO;
import com.esprit.stageback.dto.TrainerDTO;

import java.util.List;

public interface AdminCourseService {

    List<CourseFileDTO> list(Long groupeId,
                             String subject,
                             Long trainerId,
                             String query,
                             String specialite);

    List<String> subjects(Long groupeId);

    String presignedUrl(Long id, int minutes);

    void delete(Long id);

    Meta meta();

    record Meta(
            List<GroupDTO> groups,
            List<String> subjects,
            List<TrainerDTO> trainers,
            List<String> specialites
    ) {}
}
