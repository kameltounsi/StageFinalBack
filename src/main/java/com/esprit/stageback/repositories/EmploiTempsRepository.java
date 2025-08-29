package com.esprit.stageback.repositories;

import com.esprit.stageback.dto.WeeklyItemDto;
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

    @Query("""
           SELECT e FROM EmploiTemps e
           WHERE e.groupe.id = :groupeId
             AND e.date BETWEEN :start AND :end
           ORDER BY e.date ASC, e.heureDebut ASC
           """)
    List<EmploiTemps> findPlanning(@Param("groupeId") Long groupeId,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);
    // ✅ NOUVEAU : emploi du temps d’un formateur
    @Query("""
           select e
           from EmploiTemps e
           where e.formateur.id = :trainerId and e.date between :start and :end
           order by e.date, e.heureDebut
           """)
    List<EmploiTemps> findTrainerPlanning(@Param("trainerId") Long trainerId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    List<EmploiTemps> findByFormateurIdAndDateBetweenOrderByDateAscHeureDebutAsc(
            Long formateurId, LocalDate start, LocalDate end
    );
    @Query("""
        select e
        from EmploiTemps e
        where e.formateur.id = :trainerId
          and e.date between :start and :end
        order by e.date asc, e.heureDebut asc
    """)
    List<EmploiTemps> findWeeklyForTrainer(
            @Param("trainerId") Long trainerId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        select e from EmploiTemps e
        where e.groupe.id = :groupId
          and e.date between :start and :end
        order by e.date asc, e.heureDebut asc
    """)
    List<EmploiTemps> findWeeklyForGroup(@Param("groupId") Long groupId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    List<EmploiTemps> findByFormateur_IdAndDateBetween(Long trainerId, LocalDate start, LocalDate end);
}
