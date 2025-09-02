package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // utilisé pour pré-remplir la feuille
    List<Note> findByEtudiant_IdInAndDateAndMatiereIgnoreCase(
            Collection<Long> etudiantIds,
            LocalDate date,
            String matiere
    );
}
