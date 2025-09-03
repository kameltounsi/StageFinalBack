// src/main/java/com/esprit/stageback/repositories/CourseFileRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {
    List<CourseFile> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<CourseFile> findByTrainerIdAndGroupeIdOrderByCreatedAtDesc(Long trainerId, Long groupeId);
}
