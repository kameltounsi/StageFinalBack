// src/main/java/com/esprit/stageback/services/StudentCourseService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.CourseFileDTO;
import com.esprit.stageback.dto.SubjectTeacherDTO;

import java.util.List;
import java.util.Map;

public interface StudentCourseService {
    List<String> mySubjects(String studentEmail);
    List<CourseFileDTO> myCourses(String studentEmail, String subject);
    String presignedUrl(String studentEmail, Long courseId, int minutes);

    /** NEW: mapping sujet -> (nom + avatar du prof) */
    Map<String, SubjectTeacherDTO> subjectsMeta(String studentEmail);
}
