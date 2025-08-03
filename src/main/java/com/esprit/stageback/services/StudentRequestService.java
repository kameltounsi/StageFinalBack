package com.esprit.stageback.services;

import com.esprit.stageback.entities.StudentRequest;
import com.esprit.stageback.repositories.StudentRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudentRequestService {

    private final StudentRequestRepository repository;
    private final CloudinaryService cloudinaryService;

    public StudentRequestService(StudentRequestRepository repository, CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.cloudinaryService = cloudinaryService;
    }

    public StudentRequest submitRequest(String email, String specialite, MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(image);
        }

        StudentRequest request = StudentRequest.builder()
                .email(email)
                .specialite(specialite)
                .profilePicture(imageUrl)
                .status("PENDING")
                .build();

        return repository.save(request);
    }
}
