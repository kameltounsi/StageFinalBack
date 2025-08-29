// src/main/java/com/esprit/stageback/services/StudentScheduleService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.StudentWeeklyItemDto;
import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.repositories.EmploiTempsRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentScheduleService {

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END   = LocalTime.of(13, 0);

    private final UserRepository userRepository;
    private final EmploiTempsRepository edtRepository;

    public List<StudentWeeklyItemDto> getForCurrentStudent(LocalDate start, LocalDate end) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }
        String email = auth.getName();

        Long groupId = userRepository.findGroupIdByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student group not found"));

        return getForGroup(groupId, start, end);
    }

    public List<StudentWeeklyItemDto> getForGroup(Long groupId, LocalDate start, LocalDate end) {
        return edtRepository
                .findWeeklyForGroup(groupId, start, end)
                .stream()
                .flatMap(edt -> splitAroundLunch(edt).stream())
                .map(StudentWeeklyItemDto::fromEntity)
                .toList();
    }

    private List<EmploiTemps> splitAroundLunch(EmploiTemps e) {
        var start = e.getHeureDebut();
        var end   = e.getHeureFin();

        if (end.compareTo(LUNCH_START) <= 0 || start.compareTo(LUNCH_END) >= 0) {
            return List.of(e);
        }

        List<EmploiTemps> parts = new ArrayList<>(2);
        if (start.isBefore(LUNCH_START)) {
            parts.add(copyWithTimes(e, start, LUNCH_START));
        }
        if (end.isAfter(LUNCH_END)) {
            parts.add(copyWithTimes(e, LUNCH_END, end));
        }
        return parts.isEmpty() ? List.of() : parts;
    }

    private EmploiTemps copyWithTimes(EmploiTemps src, LocalTime start, LocalTime end) {
        EmploiTemps c = new EmploiTemps();
        c.setId(src.getId());
        c.setDate(src.getDate());
        c.setMatiere(src.getMatiere());
        c.setSalle(src.getSalle());
        c.setGroupe(src.getGroupe());
        c.setFormateur(src.getFormateur());
        c.setHeureDebut(start);
        c.setHeureFin(end);
        return c;
    }
}
