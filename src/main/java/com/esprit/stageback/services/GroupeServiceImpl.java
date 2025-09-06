package com.esprit.stageback.services;

import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.PresenceRepository;
import com.esprit.stageback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupeServiceImpl implements GroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    // ⚠️ Assure-toi d’avoir ces repositories avec des méthodes de suppression bulk
    private final PresenceRepository presenceRepository; // void deleteByEtudiantIds(List<Long> ids);
    private final NoteRepository noteRepository;         // void deleteByEtudiantIds(List<Long> ids);

    // ==========================
    //        CRUD de base
    // ==========================

    @Override
    @Transactional
    public Groupe createGroup(String specialite, String niveau, List<Long> trainerIds, List<Long> studentIds) {
        // Normaliser
        String level = (niveau == null ? "" : niveau.trim().toUpperCase());
        if (!level.equals("A") && !level.equals("B")) {
            throw new IllegalArgumentException("Invalid niveau: expected A or B");
        }

        // Canoniser la spécialité (reprendre exactement celle stockée si elle existe déjà)
        String specCanonical = groupeRepository.findAll().stream()
                .map(Groupe::getSpecialite)
                .filter(Objects::nonNull)
                .filter(s -> s.equalsIgnoreCase(specialite))
                .findFirst()
                .orElse(specialite);

        // Générer code + baseNom
        String code = generateCodeFromSpecialite(specCanonical);
        String baseNom = code + " " + level;

        // Chercher tous les groupes qui commencent par baseNom
        List<Groupe> existingGroups = groupeRepository.findAll().stream()
                .filter(g -> g.getNom() != null && g.getNom().startsWith(baseNom))
                .toList();

        // Trouver le plus grand suffixe
        int maxSuffix = 0;
        for (Groupe g : existingGroups) {
            String nom = g.getNom().trim();
            if (nom.equals(baseNom)) {
                maxSuffix = Math.max(maxSuffix, 1);
            } else if (nom.matches(baseNom + " \\d+")) {
                String suffixStr = nom.substring(baseNom.length()).trim();
                try {
                    int suffix = Integer.parseInt(suffixStr);
                    maxSuffix = Math.max(maxSuffix, suffix);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Générer le nom final
        String finalNom = existingGroups.isEmpty() ? baseNom : baseNom + " " + (maxSuffix + 1);

        // Créer et sauvegarder
        Groupe groupe = Groupe.builder()
                .nom(finalNom)
                .specialite(specCanonical)
                .trainerCapacity(2)   // “restant”
                .studentCapacity(25)  // “restant”
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
    @Transactional
    public void deleteGroup(Long id) {
        groupeRepository.deleteById(id);
    }

    @Override
    public List<Groupe> findGroupsBySpecialiteAndLevel(String specialite, String level) {
        String code = (level == null ? "" : level.trim().toUpperCase());
        String spec = specialite == null ? "" : specialite.trim();

        return groupeRepository.findAll().stream()
                .filter(g -> g.getSpecialite() != null
                        && g.getSpecialite().equalsIgnoreCase(spec)
                        && g.getNom() != null
                        && g.getNom().contains(code))
                .toList();
    }

    // ==========================
    //   Random Affect (RESET)
    // ==========================

    /**
     * Réinitialise notes & présences des étudiants d'une spécialité,
     * enlève leurs anciennes affectations, puis les répartit équitablement
     * sur les groupes "spécialité + niveau" (A/B) en round-robin.
     */
    @Override
    @Transactional
    public RandomAffectReport randomAssignStudents(String specialite, String niveau) {
        if (specialite == null || specialite.isBlank()) {
            throw new IllegalArgumentException("specialite is required");
        }
        if (niveau == null || niveau.isBlank()) {
            throw new IllegalArgumentException("niveau is required (A/B)");
        }

        String level = niveau.trim().toUpperCase();
        if (!level.equals("A") && !level.equals("B")) {
            throw new IllegalArgumentException("Invalid niveau: expected A or B");
        }

        // Canoniser la spécialité sur base de la DB si possible
        String specCanonical = groupeRepository.findAll().stream()
                .map(Groupe::getSpecialite)
                .filter(Objects::nonNull)
                .filter(s -> s.equalsIgnoreCase(specialite))
                .findFirst()
                .orElse(specialite);

        // baseNom = "WD A" / "WD B"...
        String codeSpec = generateCodeFromSpecialite(specCanonical);
        String baseNom = codeSpec + " " + level;

        // Groupes cibles: même spécialité & nom commence par baseNom (“WD A”, “WD A 2”...)
        List<Groupe> targetGroups = groupeRepository.findAll().stream()
                .filter(g -> g.getSpecialite() != null
                        && g.getSpecialite().equalsIgnoreCase(specCanonical)
                        && g.getNom() != null
                        && g.getNom().startsWith(baseNom))
                .collect(Collectors.toCollection(ArrayList::new));

        if (targetGroups.isEmpty()) {
            // Ajout d’info utile dans le message
            List<String> suggestions = groupeRepository.findAll().stream()
                    .filter(g -> g.getSpecialite() != null && g.getSpecialite().equalsIgnoreCase(specCanonical))
                    .map(Groupe::getNom)
                    .sorted()
                    .toList();

            throw new IllegalStateException(
                    "Aucun groupe trouvé pour %s niveau %s (baseNom=%s). Groupes de la spécialité existants: %s"
                            .formatted(specCanonical, level, baseNom, suggestions));
        }

        // Étudiants de la spécialité (quel que soit le groupe actuel)
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Roles.STUDENT
                        && u.getSpecialite() != null
                        && u.getSpecialite().equalsIgnoreCase(specCanonical))
                .collect(Collectors.toCollection(ArrayList::new));

        // Supprimer Notes & Présences (bulk) puis désaffecter
        if (!students.isEmpty()) {
            List<Long> ids = students.stream().map(User::getId).toList();
            noteRepository.deleteByEtudiantIds(ids);
            presenceRepository.deleteByEtudiantIds(ids);
        }

        for (User s : students) {
            s.setStudentGroupe(null);
        }
        userRepository.saveAll(students);

        // Réinitialiser l’état visible des groupes cibles
        for (Groupe g : targetGroups) {
            if (g.getStudents() != null) {
                g.getStudents().clear(); // côté inverse; owning = User.studentGroupe
            }
            g.setStudentCapacity(25); // modèle restant
        }
        groupeRepository.saveAll(targetGroups);

        // Ordonner les groupes: "A" (1), "A 2"(2), "A 3"(3)...
        targetGroups.sort((g1, g2) -> {
            int s1 = extractNumericSuffix(g1.getNom(), baseNom);
            int s2 = extractNumericSuffix(g2.getNom(), baseNom);
            return Integer.compare(s1, s2);
        });

        // Shuffle + round-robin en respectant la capacité restante
        Collections.shuffle(students);
        Map<Long, Integer> remaining = new HashMap<>();
        for (Groupe g : targetGroups) {
            remaining.put(g.getId(), Math.max(0, g.getStudentCapacity()));
        }

        int gi = 0;
        for (User s : students) {
            int loops = 0;
            while (loops < targetGroups.size() && remaining.get(targetGroups.get(gi).getId()) <= 0) {
                gi = (gi + 1) % targetGroups.size();
                loops++;
            }
            Groupe g = targetGroups.get(gi);
            s.setStudentGroupe(g);
            userRepository.save(s);

            // 🔧 FIX: computeIfPresent prend (key, value) et on reste en Integer
            remaining.computeIfPresent(g.getId(), (k, v) -> Integer.max(0, v - 1));

            gi = (gi + 1) % targetGroups.size();
        }

        // Mettre à jour les capacités restantes affichées
        Map<Long, Long> perGroupCount =
                students.stream()
                        .filter(u -> u.getStudentGroupe() != null)
                        .collect(Collectors.groupingBy(u -> u.getStudentGroupe().getId(), Collectors.counting()));

        for (Groupe g : targetGroups) {
            long used = perGroupCount.getOrDefault(g.getId(), 0L);
            int rest = Math.max(0, g.getStudentCapacity() - (int) used);
            g.setStudentCapacity(rest);
        }
        groupeRepository.saveAll(targetGroups);

        // Rapport simple
        Map<String, Integer> assignedPerGroup = new LinkedHashMap<>();
        for (Groupe g : targetGroups) {
            int c = perGroupCount.getOrDefault(g.getId(), 0L).intValue();
            assignedPerGroup.put(g.getNom(), c);
        }

        return new RandomAffectReport(specCanonical, level, students.size(), targetGroups.size(), assignedPerGroup);
    }

    // ==========================
    //        Utils & DTO
    // ==========================

    // Insensible à la casse (clés en minuscule)
    private String generateCodeFromSpecialite(String specialite) {
        if (specialite == null || specialite.isBlank()) {
            throw new IllegalArgumentException("specialite is required");
        }
        String key = specialite.trim().toLowerCase();

        Map<String, String> codes = Map.ofEntries(
                Map.entry("cybersecurity & ethical hacking", "C&EH"),
                Map.entry("web development", "WD"),
                Map.entry("mobile application development", "MAD"),
                Map.entry("graphic design & multimedia", "GDM"),
                Map.entry("digital marketing & social media management", "DM"),
                Map.entry("electrical installation & building wiring", "EI"),
                Map.entry("plumbing & sanitary installations", "PSI"),
                Map.entry("masonry & concrete works", "MC"),
                Map.entry("carpentry & woodworking", "CW"),
                Map.entry("hvac systems", "HVAC"),
                Map.entry("accounting & financial management", "AFM"),
                Map.entry("human resources management", "HRM"),
                Map.entry("office administration & secretarial studies", "OAS"),
                Map.entry("sales & commercial techniques", "SCT"),
                Map.entry("logistics & supply chain management", "LSCM")
        );
        String code = codes.get(key);
        if (code != null) return code;

        String clean = specialite.trim();
        int len = Math.min(3, clean.length());
        return clean.substring(0, len).toUpperCase();
    }

    /** Extrait le suffixe numérique de "baseNom X" → X ; si "baseNom" seul → 1 ; sinon → MAX_VALUE */
    private int extractNumericSuffix(String nom, String baseNom) {
        if (nom == null) return Integer.MAX_VALUE;
        String rest = nom.trim();
        if (rest.equals(baseNom)) return 1;
        if (rest.startsWith(baseNom + " ")) {
            String suffix = rest.substring((baseNom + " ").length()).trim();
            try { return Integer.parseInt(suffix); } catch (Exception ignored) {}
        }
        return Integer.MAX_VALUE;
    }

    @Data
    @AllArgsConstructor
    public static class RandomAffectReport {
        private String specialite;
        private String niveau;
        private int totalStudents;
        private int totalGroups;
        private Map<String, Integer> assignedPerGroup;
    }
}
