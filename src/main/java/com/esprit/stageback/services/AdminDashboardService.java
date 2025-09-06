package com.esprit.stageback.services;

import com.esprit.stageback.dto.AdminOverviewDTO;
import com.esprit.stageback.dto.GroupStatusRowDTO;
import com.esprit.stageback.dto.SpecialiteCountDTO;
import com.esprit.stageback.dto.TrainersBySpecialiteDTO;

import java.util.List;

public interface AdminDashboardService {
    AdminOverviewDTO getOverview();
    List<GroupStatusRowDTO> getGroupStatus();
    List<TrainersBySpecialiteDTO> getTrainersBySpecialite();

    List<SpecialiteCountDTO> getStudentsBySpecialite() ;

}
