// TrainerScheduleService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.WeeklyItemDto;
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
public class TrainerScheduleService {

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END   = LocalTime.of(13, 0);

    private final UserRepository userRepository;
    private final EmploiTempsRepository edtRepository;

    public List<WeeklyItemDto> getForCurrentTrainer(LocalDate start, LocalDate end) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }
        String email = auth.getName();
        Long trainerId = userRepository.findIdByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return getForTrainer(trainerId, start, end);
    }

    public List<WeeklyItemDto> getForTrainer(Long trainerId, LocalDate start, LocalDate end) {
        return edtRepository
                .findWeeklyForTrainer(trainerId, start, end)
                .stream()
                .flatMap(edt -> splitAroundLunch(edt).stream())
                .map(WeeklyItemDto::fromEntity)
                .toList();
    }

    /**
     * Si un cours traverse 12:00–13:00, on le découpe en 2 créneaux :
     * [debut → 12:00] et [13:00 → fin]. Sinon on renvoie tel quel.
     */
    private List<EmploiTemps> splitAroundLunch(EmploiTemps e) {
        var start = e.getHeureDebut();
        var end   = e.getHeureFin();

        // Entièrement avant ou après la pause → rien à faire
        if (end.compareTo(LUNCH_START) <= 0 || start.compareTo(LUNCH_END) >= 0) {
            return List.of(e);
        }

        List<EmploiTemps> parts = new ArrayList<>(2);

        // Partie du matin (si elle existe)
        if (start.isBefore(LUNCH_START)) {
            parts.add(copyWithTimes(e, start, LUNCH_START));
        }

        // Partie de l’après-midi (si elle existe)
        if (end.isAfter(LUNCH_END)) {
            parts.add(copyWithTimes(e, LUNCH_END, end));
        }

        return parts.isEmpty() ? List.of() : parts;
    }

    private EmploiTemps copyWithTimes(EmploiTemps src, LocalTime start, LocalTime end) {
        EmploiTemps c = new EmploiTemps();
        c.setId(src.getId());                // ou null si tu préfères un « faux » id
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
