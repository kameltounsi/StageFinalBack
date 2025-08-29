package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
    List<Presence> findByEmploiTemps_Id(Long emploiId);
    Optional<Presence> findByEtudiant_IdAndEmploiTemps_Id(Long studentId, Long emploiId);
}
