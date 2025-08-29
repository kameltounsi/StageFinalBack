package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupeRepository extends JpaRepository<Groupe, Long> {
    @Query("select g from Groupe g left join fetch g.students where g.id = :id")
    Optional<Groupe> findByIdWithStudents(@Param("id") Long id);
}
