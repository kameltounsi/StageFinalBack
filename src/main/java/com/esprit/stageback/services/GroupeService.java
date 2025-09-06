package com.esprit.stageback.services;

import com.esprit.stageback.entities.Groupe;

import java.util.List;
import java.util.Map;

public interface GroupeService {
    // Garde la même signature que ton implémentation réelle
    Groupe createGroup(String specialite, String niveau, List<Long> trainerIds, List<Long> studentIds);

    List<Groupe> getAllGroups();
    Groupe getGroupById(Long id);
    void deleteGroup(Long id);
    List<Groupe> findGroupsBySpecialiteAndLevel(String specialite, String level);

    // Retourne un petit rapport (DTO interne de l’impl) — on l’exporte proprement ici
    GroupeServiceImpl.RandomAffectReport randomAssignStudents(String specialite, String niveau);
}
