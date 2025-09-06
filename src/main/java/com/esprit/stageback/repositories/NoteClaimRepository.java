// src/main/java/com/esprit/stageback/repositories/NoteClaimRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.NoteClaim;
import com.esprit.stageback.entities.NoteClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NoteClaimRepository extends JpaRepository<NoteClaim, Long> {
    List<NoteClaim> findByStudent_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<NoteClaim> findByTutor_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<NoteClaim> findByStatusOrderByCreatedAtDesc(NoteClaimStatus status);
    long deleteByStudent_IdIn(Collection<Long> studentIds);
    long countByStudent_StudentGroupe_Id(Long groupId);

}
