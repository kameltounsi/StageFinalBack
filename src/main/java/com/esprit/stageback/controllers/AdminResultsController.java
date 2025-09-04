// src/main/java/com/esprit/stageback/controllers/AdminResultsController.java
package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AdminApplyResultsRequest;
import com.esprit.stageback.dto.AdminApplyResultsResponse;
import com.esprit.stageback.dto.AdminGroupResultsPreviewDTO;
import com.esprit.stageback.services.AdminResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/results")
@RequiredArgsConstructor
public class AdminResultsController {

    private final AdminResultsService service;

    @GetMapping("/preview")
    public ResponseEntity<AdminGroupResultsPreviewDTO> preview(@RequestParam Long groupeId) {
        return ResponseEntity.ok(service.previewGroupResults(groupeId));
    }

    @PostMapping("/apply")
    public ResponseEntity<AdminApplyResultsResponse> apply(@RequestBody AdminApplyResultsRequest req) {
        return ResponseEntity.ok(service.applyGroupResults(req));
    }
}
