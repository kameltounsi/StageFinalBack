// AttendanceController (version finale)
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AttendanceMarkDTO;
import com.esprit.stageback.services.AttendanceService;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/api/attendance", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" }, allowCredentials = "true")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    @GetMapping("/session/{emploiId}")
    public ResponseEntity<List<AttendanceMarkDTO>> getForSession(@PathVariable @NotNull Long emploiId) {
        return ResponseEntity.ok(attendanceService.getMarksForSession(emploiId));
    }

    @PostMapping(path = "/session/{emploiId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveForSession(
            @PathVariable @NotNull Long emploiId,
            @Valid @RequestBody List<@Valid AttendanceMarkDTO> marks,
            // username == email fourni par UserDetails
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        Long trainerId = userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        attendanceService.saveMarksForSession(emploiId, marks, trainerId);
        return ResponseEntity.noContent().build();
    }
}
