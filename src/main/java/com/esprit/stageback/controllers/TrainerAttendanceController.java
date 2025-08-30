package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.EmploiOptionDTO;
import com.esprit.stageback.dto.GroupOptionDTO;
import com.esprit.stageback.dto.PresenceMarkInput;
import com.esprit.stageback.dto.RosterViewDTO;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/trainers/me/attendance")
@RequiredArgsConstructor
public class TrainerAttendanceController {

    private final AttendanceService service;
    private final UserRepository userRepo;

    private Long me(Principal p) {
        return userRepo.findByEmail(p.getName()).orElseThrow().getId();
    }

    /* ==========================
       TODAY (raccourci pratique)
       ========================== */
    @GetMapping("/today-seances")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<EmploiOptionDTO>> todaySeances(Principal principal) {
        Long trainerId = me(principal);
        return ResponseEntity.ok(
                service.todaySeancesForTrainer(trainerId, ZoneId.of("Africa/Tunis"))
        );
    }

    /* ==========================================
       ROSTER générique (lecture/écriture)
       (valable pour aujourd’hui ET l’historique)
       ========================================== */
    @GetMapping("/sessions/{emploiId}/roster")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<RosterViewDTO> roster(
            @PathVariable Long emploiId,
            Principal principal
    ) {
        return ResponseEntity.ok(service.getRoster(emploiId, me(principal)));
    }

    @PostMapping("/sessions/{emploiId}/mark")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<RosterViewDTO> mark(
            @PathVariable Long emploiId,
            @RequestBody List<PresenceMarkInput> entries,
            Principal principal
    ) {
        return ResponseEntity.ok(service.mark(emploiId, me(principal), entries));
    }

    /* ==========================
       HISTORY (liste par intervalle)
       ========================== */
    @GetMapping("/history-sessions")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<EmploiOptionDTO>> history(
            @RequestParam String start,
            @RequestParam String end,
            Principal principal
    ) {
        Long trainerId = me(principal);
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        return ResponseEntity.ok(service.sessionsBetweenForTrainer(trainerId, s, e));
    }

    /* -----------------------------------------------------------------
       Compatibilité : routes “history-sessions/{id}/*” (même logique que
       /sessions/{id}/*, gardées pour le front existant si besoin)
       ----------------------------------------------------------------- */
    @GetMapping("/history-sessions/{emploiId}/roster")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<RosterViewDTO> historyRoster(
            @PathVariable Long emploiId,
            Principal principal
    ) {
        return ResponseEntity.ok(service.getRoster(emploiId, me(principal)));
    }

    @PostMapping("/history-sessions/{emploiId}/mark")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<RosterViewDTO> markHistory(
            @PathVariable Long emploiId,
            @RequestBody List<PresenceMarkInput> entries,
            Principal principal
    ) {
        return ResponseEntity.ok(service.mark(emploiId, me(principal), entries));
    }

    /* ==========================================
       Sélection par CLASSE -> Séances de la classe
       ========================================== */
    @GetMapping("/groups")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<GroupOptionDTO>> myGroups(Principal principal) {
        return ResponseEntity.ok(service.groupsForTrainer(me(principal)));
    }

    @GetMapping("/sessions-by-group")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<EmploiOptionDTO>> sessionsByGroup(
            @RequestParam Long groupId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Principal principal
    ) {
        Long trainerId = me(principal);
        LocalDate s = (start == null || start.isBlank()) ? null : LocalDate.parse(start);
        LocalDate e = (end == null || end.isBlank()) ? null : LocalDate.parse(end);
        return ResponseEntity.ok(service.sessionsByGroup(trainerId, groupId, s, e));
    }
}
