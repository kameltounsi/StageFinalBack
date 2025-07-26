package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Seance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeanceRepository extends JpaRepository<Seance,Long> {
}
