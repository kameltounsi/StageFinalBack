package com.esprit.stageback.controllers;
import com.esprit.stageback.entities.StudentRequest;
import com.esprit.stageback.services.StudentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

}
