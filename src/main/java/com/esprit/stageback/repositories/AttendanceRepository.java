package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmploi_Id(Long emploiId);
    void deleteByEmploi_Id(Long emploiId);
    boolean existsByEmploi_IdAndStudent_Id(Long emploiId, Long studentId);
}
