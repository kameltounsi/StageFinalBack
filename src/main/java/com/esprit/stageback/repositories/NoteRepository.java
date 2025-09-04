// src/main/java/com/esprit/stageback/repositories/NoteRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByEtudiant_IdInAndMatiere(List<Long> etudiantIds, String matiere);

    Optional<Note> findByEtudiant_IdAndMatiere(Long etudiantId, String matiere);
    // ↓ nouvelles pour l'espace étudiant
    List<Note> findByEtudiant_IdOrderByMatiereAsc(Long etudiantId);
    List<Note> findByEtudiant_EmailIgnoreCaseOrderByMatiereAsc(String email);



    @Query("select n from Note n where n.etudiant.id in :ids")
    List<Note> findByEtudiantIdIn(Collection<Long> ids);

    @Query("""
           select n from Note n
           where n.etudiant.id in :ids
             and lower(n.matiere) in :matieres
           """)
    List<Note> findByStudentIdsAndMatieresIgnoreCase(Collection<Long> ids, Collection<String> matieres);
}
