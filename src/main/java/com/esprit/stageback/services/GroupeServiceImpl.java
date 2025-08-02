package com.esprit.stageback.services;

import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupeServiceImpl implements GroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    @Override
    public Groupe createGroup(String specialite, String niveau, List<Long> trainerIds, List<Long> studentIds) {
        // Mapping spécialités → codes abrégés
        Map<String, String> specialiteCodes = Map.ofEntries(
                Map.entry("Cybersecurity & Ethical Hacking", "C&EH"),
                Map.entry("Web Development", "WD"),
                Map.entry("Mobile Application Development", "MAD"),
                Map.entry("Graphic Design & Multimedia", "GDM"),
                Map.entry("Digital Marketing & Social Media Management", "DM"),
                Map.entry("Electrical Installation & Building Wiring", "EI"),
                Map.entry("Plumbing & Sanitary Installations", "PSI"),
                Map.entry("Masonry & Concrete Works", "MC"),
                Map.entry("Carpentry & Woodworking", "CW"),
                Map.entry("HVAC Systems", "HVAC"),
                Map.entry("Accounting & Financial Management", "AFM"),
                Map.entry("Human Resources Management", "HRM"),
                Map.entry("Office Administration & Secretarial Studies", "OAS"),
                Map.entry("Sales & Commercial Techniques", "SCT"),
                Map.entry("Logistics & Supply Chain Management", "LSCM")
        );

        // Code abrégé de la spécialité
        String code = specialiteCodes.getOrDefault(specialite, specialite.substring(0, 3).toUpperCase());
        String baseNom = code + " " + niveau;

        // Compter combien existent déjà
        long count = groupeRepository.findAll().stream()
                .filter(g -> g.getNom().startsWith(baseNom))
                .count();

        String finalNom = baseNom + (count == 0 ? "" : " " + (count + 1));

        Groupe groupe = Groupe.builder()
                .nom(finalNom)
                .specialite(specialite)
                .membres(new ArrayList<>())
                .build();

        Groupe savedGroup = groupeRepository.save(groupe);

        // Affecter trainers
        if (trainerIds != null && !trainerIds.isEmpty()) {
            List<User> trainers = userRepository.findAllById(trainerIds);
            for (User trainer : trainers) {
                trainer.getGroupes().add(savedGroup);
                userRepository.save(trainer);
            }
            savedGroup.getMembres().addAll(trainers);
        }

        // Affecter students
        if (studentIds != null && !studentIds.isEmpty()) {
            List<User> students = userRepository.findAllById(studentIds);
            for (User student : students) {
                student.getGroupes().clear(); // Un seul groupe pour Student
                student.getGroupes().add(savedGroup);
                userRepository.save(student);
            }
            savedGroup.getMembres().addAll(students);
        }

        return groupeRepository.save(savedGroup);
    }

    @Override
    public List<Groupe> getAllGroups() {
        return groupeRepository.findAll();
    }

    @Override
    public Groupe getGroupById(Long id) {
        return groupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));
    }

    @Override
    public void deleteGroup(Long id) {
        groupeRepository.deleteById(id);
    }
}
