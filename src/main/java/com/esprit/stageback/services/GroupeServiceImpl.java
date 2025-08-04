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
        // Générer le code de la spécialité
        String code = generateCodeFromSpecialite(specialite);
        String baseNom = code + " " + niveau.toUpperCase();

        // Chercher tous les groupes qui commencent par baseNom
        List<Groupe> existingGroups = groupeRepository.findAll().stream()
                .filter(g -> g.getNom().startsWith(baseNom))
                .toList();

        // Trouver le plus grand suffixe
        int maxSuffix = 0;
        for (Groupe g : existingGroups) {
            String nom = g.getNom().trim();

            // Cas exact : "WD A" sans suffixe
            if (nom.equals(baseNom)) {
                maxSuffix = Math.max(maxSuffix, 1);
            }
            // Cas avec suffixe numérique : "WD A 2", "WD A 10"
            else if (nom.matches(baseNom + " \\d+")) {
                String suffixStr = nom.substring(baseNom.length()).trim();
                try {
                    int suffix = Integer.parseInt(suffixStr);
                    maxSuffix = Math.max(maxSuffix, suffix);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Générer le nom final
        String finalNom;
        if (existingGroups.isEmpty()) {
            finalNom = baseNom; // Premier groupe → WD A
        } else {
            finalNom = baseNom + " " + (maxSuffix + 1); // Exemple → WD A4
        }

        // Créer et sauvegarder le groupe
        Groupe groupe = Groupe.builder()
                .nom(finalNom)
                .specialite(specialite)
                .trainerCapacity(2)   // ✅ fixé
                .studentCapacity(25)
                .build();

        Groupe savedGroup = groupeRepository.save(groupe);

        // Affecter les formateurs
        if (trainerIds != null && !trainerIds.isEmpty()) {
            List<User> trainers = userRepository.findAllById(trainerIds);
            for (User trainer : trainers) {
                if (trainer.getTrainerGroupes() == null) {
                    trainer.setTrainerGroupes(new ArrayList<>());
                }

                if (trainer.getTrainerGroupes().size() >= 4) {
                    throw new RuntimeException("Trainer " + trainer.getFullName() + " has reached the maximum group limit (4).");
                }

                trainer.getTrainerGroupes().add(savedGroup);
                userRepository.save(trainer);
            }
        }


        // Affecter les étudiants
        if (studentIds != null && !studentIds.isEmpty()) {
            List<User> students = userRepository.findAllById(studentIds);
            for (User student : students) {
                student.setStudentGroupe(savedGroup);
                userRepository.save(student);
            }
        }

        return savedGroup;
    }


    private String generateCodeFromSpecialite(String specialite) {
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
        return specialiteCodes.getOrDefault(specialite, specialite.substring(0, 3).toUpperCase());
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
    @Override
    public List<Groupe> findGroupsBySpecialiteAndLevel(String specialite, String level) {
        String code = switch (level.toUpperCase()) {
            case "A" -> "A";
            case "B" -> "B";
            default -> level;
        };

        return groupeRepository.findAll().stream()
                .filter(g -> g.getSpecialite().equalsIgnoreCase(specialite)
                        && g.getNom().contains(code)) // on filtre par spécialité + niveau
                .toList();
    }

}
