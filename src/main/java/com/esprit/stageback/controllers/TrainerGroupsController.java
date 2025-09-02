package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.GroupSummaryDTO;
import com.esprit.stageback.services.TrainerGroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/trainer", "/api/trainers"})
@RequiredArgsConstructor
public class TrainerGroupsController {

    private final TrainerGroupsService service;

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupSummaryDTO>> myGroups(Authentication auth) {
        var email = auth.getName();
        return ResponseEntity.ok(service.myGroups(email));
    }
}
