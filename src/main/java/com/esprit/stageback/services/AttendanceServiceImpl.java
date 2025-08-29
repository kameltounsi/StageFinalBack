package com.esprit.stageback.services;

import com.esprit.stageback.dto.AttendanceMarkDTO;
import com.esprit.stageback.entities.Attendance;
import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.AttendanceRepository;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmploiTempsRepository emploiTempsRepository;
    private final UserRepository userRepository;

    @Override
    public List<AttendanceMarkDTO> getMarksForSession(Long emploiId) {
        return attendanceRepository.findByEmploi_Id(emploiId).stream()
                .map(a -> AttendanceMarkDTO.builder()
                        .studentId(a.getStudent().getId())
                        .present(a.isPresent())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void saveMarksForSession(Long emploiId, List<AttendanceMarkDTO> marks, Long trainerId) {
        EmploiTemps emploi = emploiTempsRepository.findById(emploiId)
                .orElseThrow(() -> new IllegalArgumentException("Séance introuvable"));

        // (Sécurité) s’assurer que le formateur qui marque les présences est bien le propriétaire de la séance
        if (trainerId != null && emploi.getFormateur() != null
                && !Objects.equals(emploi.getFormateur().getId(), trainerId)) {
            throw new IllegalStateException("Vous ne pouvez pas modifier la présence d’une séance d’un autre formateur");
        }

        Groupe groupe = emploi.getGroupe();
        if (groupe == null) throw new IllegalStateException("La séance n’est liée à aucun groupe");

        // Map des étudiants du groupe pour valider + éviter un SELECT par étudiant
        List<User> students = Optional.ofNullable(groupe.getStudents()).orElseGet(Collections::emptyList);
        Map<Long, User> studentById = students.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Filtrer/valider les marks
        List<AttendanceMarkDTO> validMarks = marks == null ? List.of() :
                marks.stream()
                        .filter(m -> studentById.containsKey(m.getStudentId()))
                        .toList();

        // stratégie simple : on remplace tout
        attendanceRepository.deleteByEmploi_Id(emploiId);

        // sauvegarde bulk
        if (validMarks.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<Attendance> toSave = new ArrayList<>(validMarks.size());
        for (AttendanceMarkDTO m : validMarks) {
            User student = studentById.get(m.getStudentId()); // déjà en mémoire (pas de SELECT)
            Attendance a = Attendance.builder()
                    .emploi(emploi)
                    .student(student)
                    .present(m.isPresent())
                    .markedAt(now)
                    .build();
            toSave.add(a);
        }
        attendanceRepository.saveAll(toSave);
    }
}
