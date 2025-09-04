// src/main/java/com/esprit/stageback/services/AdminResultsServiceImpl.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Note;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.NoteClaimRepository;
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
    private final NoteClaimRepository claimRepo;
    private final CourseService courseService;

    private static final double THRESHOLD = 10.0; // fixed

    @Override
    @Transactional(readOnly = true)
    public AdminGroupResultsPreviewDTO previewGroupResults(Long groupeId) {
        final Groupe g = groupeRepo.findByIdWithStudents(groupeId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupeId));

        final List<String> expected = courseService.allowedSubjectsForGroup(groupeId);
        final List<String> expectedNorm = expected.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .toList();

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

        // Load notes; filter by expected subjects if provided
        final List<Note> notes = expectedNorm.isEmpty()
                ? noteRepo.findByEtudiantIdIn(studentIds)
                : noteRepo.findByStudentIdsAndMatieresIgnoreCase(
                studentIds, expectedNorm.stream().map(String::toLowerCase).toList());

        // Build map: studentId -> { subject(lowercase) -> moyenne }
        final Map<Long, Map<String, Double>> byStudent = new HashMap<>();
        for (Note n : notes) {
            final Long sid = n.getEtudiant().getId();
            final String key = (n.getMatiere() == null) ? "" : n.getMatiere().trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty()) continue;
            byStudent.computeIfAbsent(sid, k -> new HashMap<>()).put(key, n.getMoyenne());
        }

        final List<AdminStudentResultDTO> studentRows = new ArrayList<>();
        long admitted = 0, rejected = 0, incomplete = 0;

        final int expectedCount = expected.size();

        for (User s : students) {
            final Map<String, Double> subjAvg = new LinkedHashMap<>();
            final List<String> missing = new ArrayList<>();

            // If no expected subjects configured, mark as INCOMPLETE safely
            if (expectedCount == 0) {
                studentRows.add(AdminStudentResultDTO.builder()
                        .studentId(s.getId())
                        .studentName(s.getFullName())
                        .studentEmail(s.getEmail())
                        .subjectAverages(subjAvg)
                        .missingSubjects(List.of())
                        .overall(null)
                        .status("INCOMPLETE")
                        .build());
                incomplete++;
                continue;
            }

            for (String m : expected) {
                final String key = m == null ? "" : m.trim().toLowerCase(Locale.ROOT);
                final Double v = byStudent.getOrDefault(s.getId(), Map.of()).get(key);
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
                // expectedCount > 0 here (guarded above)
                overall = round2(sum / expectedCount);
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

        // Resolve or create target group (A -> ensure/create B; B -> error)
        final Groupe source = groupeRepo.findById(prev.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Source group not found: " + prev.getGroupId()));

        final Groupe target = (req.getTargetGroupId() != null)
                ? groupeRepo.findById(req.getTargetGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Target group not found: " + req.getTargetGroupId()))
                : ensureOrCreateNextGroup(source);

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

        // === YEAR ROLLOVER PURGE (notes + claims for ALL students of source group) ===
        final Collection<Long> allStudentIds = prev.getStudents().stream()
                .map(AdminStudentResultDTO::getStudentId)
                .collect(Collectors.toList());

        final long purgedClaims = allStudentIds.isEmpty() ? 0L : claimRepo.deleteByStudent_IdIn(allStudentIds);
        final long purgedNotes  = allStudentIds.isEmpty() ? 0L : noteRepo.deleteByEtudiant_IdIn(allStudentIds);

        return AdminApplyResultsResponse.builder()
                .sourceGroupId(prev.getGroupId())
                .targetGroupId(target.getId())
                .targetGroupName(targetName)
                .movedCount(moved)
                .stayedCount(stayed)
                .purgedNotesCount(purgedNotes)
                .purgedClaimsCount(purgedClaims)
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
        final Pattern pA = Pattern.compile("(?i)\\bA(\\d*)\\b");
        final Matcher mA = pA.matcher(n);
        if (mA.find()) {
            return mA.replaceFirst("B$1");
        }
        final Pattern pB = Pattern.compile("(?i)\\bB(\\d*)\\b");
        if (pB.matcher(n).find()) {
            return null; // no next level
        }
        return null; // no A/B marker
    }

    private Optional<Long> findGroupId(String specialite, String groupName) {
        if (specialite == null || groupName == null) return Optional.empty();
        return groupeRepo.findBySpecialiteIgnoreCaseAndNomIgnoreCase(
                specialite.trim(), groupName.trim()).map(Groupe::getId);
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
                    Groupe g = new Groupe();
                    g.setNom(nextName);
                    g.setSpecialite(source.getSpecialite());
                    return groupeRepo.save(g);
                });
    }
}
