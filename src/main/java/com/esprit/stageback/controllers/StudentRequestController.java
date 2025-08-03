package com.esprit.stageback.controllers;
import com.esprit.stageback.entities.StudentRequest;
import com.esprit.stageback.services.StudentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/requests")
public class StudentRequestController {

    private final StudentRequestService requestService;

    public StudentRequestController(StudentRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<StudentRequest> submitRequest(
            @RequestParam String email,
            @RequestParam String specialite,
            @RequestParam(required = false) MultipartFile image
    ) {
        StudentRequest request = requestService.submitRequest(email, specialite, image);
        return ResponseEntity.ok(request);
    }
}
