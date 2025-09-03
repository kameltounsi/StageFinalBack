// src/main/java/com/esprit/stageback/repositories/CourseFileRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {

    // === Déjà utilisés côté trainer ===
    List<CourseFile> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<CourseFile> findByTrainerIdAndGroupeIdOrderByCreatedAtDesc(Long trainerId, Long groupeId);

    // === Étudiant (utiliser le champ groupeId présent dans l'entité) ===
    List<CourseFile> findByGroupeIdOrderByCreatedAtDesc(Long groupeId);
    List<CourseFile> findByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(Long groupeId, String subject);

    // Dernier fichier d’un sujet (pour déterminer le prof du sujet)
    CourseFile findTopByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(Long groupeId, String subject);

    // Liste des matières distinctes d’un groupe (stockées en lower côté requête)
    @Query("select distinct lower(cf.subject) from CourseFile cf where cf.groupeId = :groupeId")
    List<String> findDistinctSubjectsForGroup(Long groupeId);
}
