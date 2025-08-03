package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.RequestStatus;
import com.esprit.stageback.entities.StudentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long> {
    boolean existsByEmail(String email);
    List<StudentRequest> findByStatus(RequestStatus status);


}
