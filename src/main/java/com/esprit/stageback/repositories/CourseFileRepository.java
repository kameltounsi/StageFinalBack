package com.esprit.stageback.repositories;

import com.esprit.stageback.dto.GroupDTO;
import com.esprit.stageback.entities.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseFileRepository extends JpaRepository<CourseFile, Long>, JpaSpecificationExecutor<CourseFile> {

    // --- existants (trainer / student) ---
    List<CourseFile> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<CourseFile> findByTrainerIdAndGroupeIdOrderByCreatedAtDesc(Long trainerId, Long groupeId);

    List<CourseFile> findByGroupeIdOrderByCreatedAtDesc(Long groupeId);
    List<CourseFile> findByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(Long groupeId, String subject);

    CourseFile findTopByGroupeIdAndSubjectIgnoreCaseOrderByCreatedAtDesc(Long groupeId, String subject);

    // --- META: Groupes distincts trouvés dans CourseFile ---
    // GroupDTO = (Long id, String nom, String specialite, List<StudentDTO> students)
    @Query("""
           select distinct new com.esprit.stageback.dto.GroupDTO(
               c.groupeId,
               c.groupeName,
               null,
               null
           )
           from CourseFile c
           where c.groupeId is not null
             and c.groupeName is not null
           order by lower(c.groupeName)
           """)
    List<GroupDTO> findDistinctGroupeNames();

    // --- META: Sujets globaux distincts (normalisés & non vides) ---
    @Query("""
           select distinct lower(trim(c.subject))
           from CourseFile c
           where c.subject is not null and trim(c.subject) <> ''
           order by lower(trim(c.subject))
           """)
    List<String> findDistinctSubjects();

    // --- Sujets distincts d’un groupe précis (normalisés & non vides) ---
    @Query("""
           select distinct lower(trim(cf.subject))
           from CourseFile cf
           where cf.groupeId = :groupeId
             and cf.subject is not null and trim(cf.subject) <> ''
           order by lower(trim(cf.subject))
           """)
    List<String> findDistinctSubjectsForGroup(@Param("groupeId") Long groupeId);

    // --- Sujets distincts (filtrés par groupe si fourni, sinon tous) ---
    @Query("""
           select distinct lower(trim(c.subject))
           from CourseFile c
           where (:gid is null or c.groupeId = :gid)
             and c.subject is not null and trim(c.subject) <> ''
           order by lower(trim(c.subject))
           """)
    List<String> findDistinctSubjectsForGroupOrAll(@Param("gid") Long gid);
}
