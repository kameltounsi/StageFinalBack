package com.esprit.stageback.controllers;
import com.esprit.stageback.entities.RequestStatus;
import com.esprit.stageback.entities.StudentRequest;
import com.esprit.stageback.services.StudentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class StudentRequestController {

    private final StudentRequestService requestService;

    public StudentRequestController(StudentRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(
            @RequestParam String email,
            @RequestParam String specialite,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            StudentRequest request = requestService.submitRequest(email, specialite, image);
            return ResponseEntity.ok(request);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(
                    java.util.Map.of("message", e.getReason())
            );
        }
    }
    // ✅ Lister les requêtes en attente
    @GetMapping("/pending")
    public ResponseEntity<List<StudentRequest>> getPendingRequests() {
        List<StudentRequest> pendingRequests = requestService.getRequestsByStatus(RequestStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }

    // ✅ Mettre à jour le statut (APPROVED ou REJECTED)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            RequestStatus status = RequestStatus.valueOf(body.get("status").toUpperCase());
            StudentRequest updatedRequest = requestService.updateRequestStatus(id, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Status updated successfully",
                    "request", updatedRequest
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status value"));
        }
    }

}
