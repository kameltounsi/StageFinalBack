// src/main/java/com/esprit/stageback/services/CourseService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    CourseFileDTO uploadCourse(String trainerEmail, Long trainerId, Long groupeId, String subject, MultipartFile pdf);
    List<CourseFileDTO> myCourses(Long trainerId, Long groupeId); // groupeId optionnel (null = tous)
    void deleteCourse(String trainerEmail, Long trainerId, Long id);
    String presignedUrl(Long trainerId, Long id, int minutes);
    List<String> allowedSubjectsForGroup(Long groupeId);
}
