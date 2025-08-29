package com.esprit.stageback.services;

import com.esprit.stageback.dto.AttendanceMarkDTO;

import java.util.List;

public interface AttendanceService {
    List<AttendanceMarkDTO> getMarksForSession(Long emploiId);
    void saveMarksForSession(Long emploiId, List<AttendanceMarkDTO> marks, Long trainerId);
}
