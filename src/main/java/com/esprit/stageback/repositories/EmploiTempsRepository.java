package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.EmploiTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface EmploiTempsRepository extends JpaRepository<EmploiTemps, Long> {

    // Retourne tous les emplois du temps d’une semaine (entre deux dates)
    List<EmploiTemps> findByGroupeId(Long groupeId);
    List<EmploiTemps> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Overlap salle au même jour
    @Query("""
        select (count(e) > 0) from EmploiTemps e
        where e.date = :date
          and e.salle = :salle
          and (e.heureDebut < :heureFin and e.heureFin > :heureDebut)
    """)
    boolean existsSalleOverlap(@Param("date") LocalDate date,
                               @Param("salle") String salle,
                               @Param("heureDebut") LocalTime heureDebut,
                               @Param("heureFin") LocalTime heureFin);

    // Overlap formateur au même jour
    @Query("""
        select (count(e) > 0) from EmploiTemps e
        where e.date = :date
          and e.formateur.id = :formateurId
          and (e.heureDebut < :heureFin and e.heureFin > :heureDebut)
    """)
    boolean existsFormateurOverlap(@Param("date") LocalDate date,
                                   @Param("formateurId") Long formateurId,
                                   @Param("heureDebut") LocalTime heureDebut,
                                   @Param("heureFin") LocalTime heureFin);
}
