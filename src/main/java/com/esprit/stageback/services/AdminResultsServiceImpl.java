// src/main/java/com/esprit/stageback/services/AdminResultsServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.NoteRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminResultsServiceImpl implements AdminResultsService {

    private final GroupeRepository groupeRepo;
    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final CourseService courseService;

    private static final double THRESHOLD = 10.0; // fixed

    @Override
    @Transactional(readOnly = true)
    public AdminGroupResultsPreviewDTO previewGroupResults(Long groupeId) {
        final Groupe g = groupeRepo.findByIdWithStudents(groupeId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupeId));

        final List<String> expected = courseService.allowedSubjectsForGroup(groupeId);
        final List<String> expectedNorm = expected.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).toList();

        final List<User> students = userRepo.findStudentsByGroupeId(groupeId);
        if (students.isEmpty()) {
            final String nextNameEmpty = nextLevelNameTwoLevels(g.getNom()); // A->B, B->null
            final Optional<Long> nextIdEmpty = findGroupId(g.getSpecialite(), nextNameEmpty);

            return AdminGroupResultsPreviewDTO.builder()
                    .groupId(g.getId())
                    .groupName(g.getNom())
                    .specialite(g.getSpecialite())
                    .expectedSubjects(expected)
                    .students(List.of())
                    .admittedCount(0)
                    .refusedCount(0)
                    .incompleteCount(0)
                    .allComplete(true)
                    .suggestedNextGroupName(nextNameEmpty)
                    .suggestedNextGroupId(nextIdEmpty.orElse(null))
                    .build();
        }

        final List<Long> studentIds = students.stream().map(User::getId).toList();
        final List<Note> notes = expectedNorm.isEmpty()
                ? noteRepo.findByEtudiantIdIn(studentIds)
                : noteRepo.findByStudentIdsAndMatieresIgnoreCase(
                studentIds, expectedNorm.stream().map(String::toLowerCase).toList());

        final Map<Long, Map<String, Double>> map = new HashMap<>();
        for (Note n : notes) {
            final Long sid = n.getEtudiant().getId();
            final String key = (n.getMatiere() == null) ? "" : n.getMatiere().trim();
            if (key.isEmpty()) continue;
            map.computeIfAbsent(sid, k -> new HashMap<>()).put(key, n.getMoyenne());
        }

        final List<AdminStudentResultDTO> studentRows = new ArrayList<>();
        long admitted = 0, rejected = 0, incomplete = 0;

        for (User s : students) {
            final Map<String, Double> subjAvg = new LinkedHashMap<>();
            final List<String> missing = new ArrayList<>();

            for (String m : expected) {
                final Double v = map.getOrDefault(s.getId(), Map.of()).get(m);
                subjAvg.put(m, v);
                if (v == null) missing.add(m);
            }

            final String status;
            final Double overall;
            if (!missing.isEmpty()) {
                status = "INCOMPLETE";
                overall = null;
                incomplete++;
            } else {
                final double sum = subjAvg.values().stream().mapToDouble(Double::doubleValue).sum();
                overall = round2(sum / expected.size());
                if (overall >= THRESHOLD) {
                    status = "ADMITTED";
                    admitted++;
                } else {
                    status = "REJECTED";
                    rejected++;
                }
            }

            studentRows.add(AdminStudentResultDTO.builder()
                    .studentId(s.getId())
                    .studentName(s.getFullName())
                    .studentEmail(s.getEmail())
                    .subjectAverages(subjAvg)
                    .missingSubjects(missing)
                    .overall(overall)
                    .status(status)
                    .build());
        }

        final String nextName = nextLevelNameTwoLevels(g.getNom()); // A->B, B->null
        final Optional<Long> nextId = findGroupId(g.getSpecialite(), nextName);

        return AdminGroupResultsPreviewDTO.builder()
                .groupId(g.getId())
                .groupName(g.getNom())
                .specialite(g.getSpecialite())
                .expectedSubjects(expected)
                .students(studentRows)
                .admittedCount(admitted)
                .refusedCount(rejected)
                .incompleteCount(incomplete)
                .allComplete(incomplete == 0)
                .suggestedNextGroupName(nextName)
                .suggestedNextGroupId(nextId.orElse(null))
                .build();
    }

    @Override
    @Transactional
    public AdminApplyResultsResponse applyGroupResults(AdminApplyResultsRequest req) {
        Objects.requireNonNull(req, "request is null");
        Objects.requireNonNull(req.getGroupeId(), "groupeId is required");

        final AdminGroupResultsPreviewDTO prev = previewGroupResults(req.getGroupeId());
        if (!prev.isAllComplete()) {
            throw new IllegalStateException("Some grades are incomplete. Please finish grading first.");
        }

        // Resolve or create target group
        final Groupe source = groupeRepo.findById(prev.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Source group not found: " + prev.getGroupId()));

        final Groupe target = (req.getTargetGroupId() != null)
                ? groupeRepo.findById(req.getTargetGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Target group not found: " + req.getTargetGroupId()))
                : ensureOrCreateNextGroup(source); // A -> (ensure/create) B ; B -> error

        final String targetName = target.getNom();

        // Move ADMITTED
        final List<Long> admittedIds = prev.getStudents().stream()
                .filter(s -> "ADMITTED".equals(s.getStatus()))
                .map(AdminStudentResultDTO::getStudentId)
                .toList();

        final Map<Long, User> batch = userRepo.findAllById(admittedIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        long moved = 0;
        for (Long sid : admittedIds) {
            final User u = batch.get(sid);
            if (u == null) continue;
            u.setStudentGroupe(target);
            moved++;
        }
        userRepo.saveAll(batch.values());

        final long stayed = prev.getStudents().size() - moved;

        return AdminApplyResultsResponse.builder()
                .sourceGroupId(prev.getGroupId())
                .targetGroupId(target.getId())
                .targetGroupName(targetName)
                .movedCount(moved)
                .stayedCount(stayed)
                .build();
    }

    // ===== Helpers =====

    private static Double round2(Double v) {
        if (v == null) return null;
        return Math.round(v * 100.0) / 100.0;
    }

    /** Two-level rule: A → B ; B → null (no next). Keeps trailing digits (e.g., "WD A3" → "WD B3"). */
    private String nextLevelNameTwoLevels(String currentName) {
        if (currentName == null) return null;
        final String n = currentName.trim();
        // Find a standalone "A<digits>" token and turn it to "B<digits>"
        final Pattern pA = Pattern.compile("(?i)\\bA(\\d*)\\b");
        final Matcher mA = pA.matcher(n);
        if (mA.find()) {
            return mA.replaceFirst("B$1");
        }
        // If it's already "B", there's no next level in the two-level system
        final Pattern pB = Pattern.compile("(?i)\\bB(\\d*)\\b");
        if (pB.matcher(n).find()) {
            return null;
        }
        // Fallback: if no A/B marker, we don't change
        return null;
    }

    private Optional<Long> findGroupId(String specialite, String groupName) {
        if (specialite == null || groupName == null) return Optional.empty();
        return groupeRepo.findBySpecialiteIgnoreCaseAndNomIgnoreCase(specialite.trim(), groupName.trim())
                .map(Groupe::getId);
    }

    /** Ensure the next group exists (A→B). If not, create it on the fly with same specialty. */
    private Groupe ensureOrCreateNextGroup(Groupe source) {
        final String nextName = nextLevelNameTwoLevels(source.getNom());
        if (nextName == null) {
            throw new IllegalArgumentException("No next level after '" + source.getNom() + "' in A/B system.");
        }
        return groupeRepo.findBySpecialiteIgnoreCaseAndNomIgnoreCase(
                        Optional.ofNullable(source.getSpecialite()).orElse("").trim(),
                        nextName.trim())
                .orElseGet(() -> {
                    // Create instantly
                    Groupe g = new Groupe();
                    g.setNom(nextName);
                    g.setSpecialite(source.getSpecialite());
                    return groupeRepo.save(g);
                });
    }
}
