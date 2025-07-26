package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note,Long> {
}
