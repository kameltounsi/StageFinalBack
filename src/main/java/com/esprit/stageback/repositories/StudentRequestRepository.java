package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.StudentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long> {
    boolean existsByEmail(String email);

}
