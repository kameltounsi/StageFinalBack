package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
}
