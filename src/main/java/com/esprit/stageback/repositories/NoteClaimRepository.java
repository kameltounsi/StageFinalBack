// src/main/java/com/esprit/stageback/repositories/NoteClaimRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.NoteClaim;
import com.esprit.stageback.entities.NoteClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NoteClaimRepository extends JpaRepository<NoteClaim, Long> {
    List<NoteClaim> findByStudent_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<NoteClaim> findByTutor_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<NoteClaim> findByStatusOrderByCreatedAtDesc(NoteClaimStatus status);
    long deleteByStudent_IdIn(Collection<Long> studentIds);
    long countByStudent_StudentGroupe_Id(Long groupId);

    /** Cas nominal : le tuteur (trainer) est renseigné sur la réclamation */
    @Query("""
        select count(c)
        from NoteClaim c
        where c.status = com.esprit.stageback.entities.NoteClaimStatus.PENDING
          and lower(c.tutor.email) = lower(:email)
    """)
    long countPendingForTrainerEmail(@Param("email") String email);

    /** Filet de sécurité : réclamations sans tuteur, mais visibles par le formateur du groupe de l'étudiant */
    @Query("""
        select count(c)
        from NoteClaim c
        join c.student s
        join s.studentGroupe g
        join g.trainers t
        where c.status = com.esprit.stageback.entities.NoteClaimStatus.PENDING
          and c.tutor is null
          and lower(t.email) = lower(:email)
    """)
    long countPendingTutorNullVisibleForTrainer(@Param("email") String email);

    /* --- Variantes paramétrées si jamais tu en as besoin ailleurs --- */

    @Query("""
        select count(c)
        from NoteClaim c
        where c.status = :status
          and lower(c.tutor.email) = lower(:email)
    """)
    long countByTutorEmailAndStatus(@Param("email") String email,
                                    @Param("status") NoteClaimStatus status);

}
