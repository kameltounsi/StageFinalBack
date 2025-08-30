// src/main/java/com/esprit/stageback/repositories/PresenceRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Presence;
import com.esprit.stageback.entities.StatutPresence;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

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
}
