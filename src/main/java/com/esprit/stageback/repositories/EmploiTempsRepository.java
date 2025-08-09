package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.EmploiTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface EmploiTempsRepository extends JpaRepository<EmploiTemps, Long> {

    // Retourne tous les emplois du temps d’un groupe
    List<EmploiTemps> findByGroupeId(Long groupeId);

    // Retourne tous les emplois du temps d’une semaine (entre deux dates)
    List<EmploiTemps> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
