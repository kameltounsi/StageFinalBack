package com.esprit.stageback.services;

import com.esprit.stageback.entities.Groupe;

import java.util.List;

public interface GroupeService {
    Groupe createGroup(String nom, String specialite, List<Long> trainerIds, List<Long> studentIds);
    List<Groupe> getAllGroups();
    Groupe getGroupById(Long id);
    void deleteGroup(Long id);
    List<Groupe> findGroupsBySpecialiteAndLevel(String specialite, String level);

}
