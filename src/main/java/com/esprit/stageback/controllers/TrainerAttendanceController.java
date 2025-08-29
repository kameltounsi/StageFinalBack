package com.esprit.stageback.controllers;
import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @GetMapping("/today-seances")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<EmploiOptionDTO>> todaySeances(Principal principal) {
        Long trainerId = me(principal);
        return ResponseEntity.ok(service.todaySeancesForTrainer(trainerId, ZoneId.of("Africa/Tunis")));
    }

    @GetMapping("/sessions/{emploiId}/roster")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<RosterViewDTO> roster(@PathVariable Long emploiId, Principal principal) {
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
}
