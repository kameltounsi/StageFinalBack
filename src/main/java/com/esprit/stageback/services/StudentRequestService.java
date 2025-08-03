package com.esprit.stageback.services;

import com.esprit.stageback.entities.RequestStatus;
import com.esprit.stageback.entities.StudentRequest;
import com.esprit.stageback.repositories.StudentRequestRepository;
import com.esprit.stageback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRequestService {

    private final StudentRequestRepository repository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public StudentRequest submitRequest(String email, String specialite, MultipartFile image) {
        // Vérifier si l'email existe déjà dans User
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered!");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(image);
        }

        StudentRequest request = StudentRequest.builder()
                .email(email)
                .specialite(specialite)
                .profilePicture(imageUrl)
                .status(RequestStatus.PENDING)
                .build();

        return repository.save(request);
    }
    public List<StudentRequest> getRequestsByStatus(RequestStatus status) {
        return repository.findByStatus(status);
    }

    public StudentRequest updateRequestStatus(Long id, RequestStatus status) {
        StudentRequest request = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        request.setStatus(status);
        return repository.save(request);
    }
}
