package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Cours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursRepository extends JpaRepository<Cours,Long> {
}
