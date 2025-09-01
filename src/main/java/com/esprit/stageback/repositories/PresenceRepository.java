// src/main/java/com/esprit/stageback/repositories/PresenceRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.dto.AdminAbsenceDetailItem;
import com.esprit.stageback.entities.Presence;
import com.esprit.stageback.entities.StatutPresence;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenceRepository extends JpaRepository<Presence, Long>, JpaSpecificationExecutor<Presence> {

    // NEW: nombre d'absences injustifiées pour un étudiant
    @Query("""
        select count(p)
        from Presence p
        where p.etudiant.id = :studentId
          and p.statut = com.esprit.stageback.entities.StatutPresence.ABSENT
          and (p.justified = false or p.justified is null)
    """)
    long countUnjustifiedByStudent(Long studentId);
    List<Presence> findByEmploiTemps_Id(Long emploiId);

    java.util.Optional<Presence> findByEtudiant_IdAndEmploiTemps_Id(Long studentId, Long emploiId);

    /* --- ABSENCES d’un étudiant (avec filtre date optionnel) --- */
    @Query("""
        select p from Presence p
        join fetch p.emploiTemps e
        left join fetch e.groupe g
        where p.etudiant.id = :studentId
          and p.statut = :absent
          and (:start is null or e.date >= :start)
          and (:end   is null or e.date <= :end)
        order by e.date desc, e.heureDebut desc
    """)
    List<Presence> findStudentAbsencesBetween(
            @Param("studentId") Long studentId,
            @Param("absent") StatutPresence absent,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        select count(p) from Presence p
        where p.etudiant.id = :studentId
          and p.statut = :absent
          and (:start is null or p.emploiTemps.date >= :start)
          and (:end   is null or p.emploiTemps.date <= :end)
    """)
    long countStudentAbsencesBetween(
            @Param("studentId") Long studentId,
            @Param("absent") StatutPresence absent,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        select count(p) from Presence p
        where p.etudiant.id = :studentId
          and p.statut = :absent
          and p.justified = true
          and (:start is null or p.emploiTemps.date >= :start)
          and (:end   is null or p.emploiTemps.date <= :end)
    """)
    long countStudentJustifiedBetween(
            @Param("studentId") Long studentId,
            @Param("absent") com.esprit.stageback.entities.StatutPresence absent,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    Optional<Presence> findByIdAndEtudiant_Id(Long id, Long etudiantId); // 👈 NEW

    // Somme des absences par étudiant (filtrée)
    @Query("""
        select u.id as studentId,
               u.fullName as studentName,
               u.email as studentEmail,
               g.id as groupId,
               g.nom as groupName,
               g.specialite as specialite,
               sum(case when p.statut = 'ABSENT' then 1 else 0 end) as totalAbsences,
               sum(case when p.statut = 'ABSENT' 
                         and (p.justified = false or p.justified is null) then 1 else 0 end) as unjustifiedAbsences
        from Presence p
        join p.etudiant u
        join p.emploiTemps e
        join e.groupe g
        where (:specialite is null or lower(trim(g.specialite)) = lower(trim(:specialite)))
          and (:groupId is null or g.id = :groupId)
          and (:start is null or e.date >= :start)
          and (:end   is null or e.date <= :end)
        group by u.id, u.fullName, u.email, g.id, g.nom, g.specialite
        """)
    List<Object[]> aggregateAbsencesByStudent(
            @Param("specialite") String specialite,
            @Param("groupId") Long groupId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // Détails des absences d’un étudiant (facultatif)
    @Query("""
        select p.id, e.date, e.heureDebut, e.heureFin, e.matiere, e.salle, p.statut
        from Presence p
        join p.emploiTemps e
        join e.groupe g
        where p.etudiant.id = :studentId
          and p.statut = 'ABSENT'
          and (:specialite is null or lower(trim(g.specialite)) = lower(trim(:specialite)))
          and (:groupId is null or g.id = :groupId)
          and (:start is null or e.date >= :start)
          and (:end   is null or e.date <= :end)
        order by e.date desc, e.heureDebut desc
        """)
    List<Object[]> findAbsenceDetailsForStudent(
            @Param("studentId") Long studentId,
            @Param("specialite") String specialite,
            @Param("groupId") Long groupId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

}
