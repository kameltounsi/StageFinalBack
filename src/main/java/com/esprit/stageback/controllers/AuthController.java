package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.AuthResponse;
import com.esprit.stageback.dto.LoginRequest;
import com.esprit.stageback.dto.UserDTO;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.RequestStatus;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.StudentRequestRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AuthService;
import com.esprit.stageback.services.CloudinaryService;
import com.esprit.stageback.services.EmailService;
import com.esprit.stageback.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final GroupeRepository groupeRepository;
    private final StudentRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthService authService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    @Qualifier("userDetailsService")
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("image") MultipartFile image) {

        String imageUrl = cloudinaryService.uploadFile(image);

        User newUser = User.builder()
                .fullName(fullname)
                .email(email)
                .password(passwordEncoder.encode(password))
                .profilePicture(imageUrl)
                .role(Roles.STUDENT)
                .build();

        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring("Bearer ".length());
        String email = jwtService.extractEmail(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Renvoie le même DTO enrichi que lors du login
        // -> expose une méthode publique dans AuthService pour réutiliser le mapping
        UserDTO dto = ((com.esprit.stageback.services.AuthServiceImpl)authService)
                .publicToDTOWithGroupIds(user);   // cf. méthode ci-dessous

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAuthentication(@RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            return ResponseEntity.ok(email != null);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAllWithGroups();

        List<UserDTO> userDTOs = users.stream().map(user -> {
            boolean isStudent = user.getRole() == Roles.STUDENT;
            boolean isTrainer = user.getRole() == Roles.TRAINER;

            String studentClass = (isStudent && user.getStudentGroupe() != null)
                    ? user.getStudentGroupe().getNom()
                    : "No class assigned";

            Long studentGroupId = (isStudent && user.getStudentGroupe() != null)
                    ? user.getStudentGroupe().getId()
                    : null;

            List<String> trainerClasses = (isTrainer && user.getTrainerGroupes() != null)
                    ? user.getTrainerGroupes().stream().map(Groupe::getNom).toList()
                    : List.of();

            List<Long> trainerGroupIds = (isTrainer && user.getTrainerGroupes() != null)
                    ? user.getTrainerGroupes().stream().map(Groupe::getId).toList()
                    : List.of();

            return UserDTO.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole() != null ? user.getRole().name() : null)
                    .profilePicture(user.getProfilePicture())
                    .studentClass(studentClass)
                    .studentGroupId(studentGroupId)
                    .trainerClasses(trainerClasses)
                    .trainerGroupIds(trainerGroupIds)
                    .build();
        }).toList();

        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Roles.valueOf(role.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Role updated successfully"));
    }

    @PostMapping("/add-user")
    public ResponseEntity<Map<String, String>> addUser(
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("role") Roles role,
            @RequestParam(value = "specialite", required = false) String specialite,
            @RequestParam(value = "groupeId", required = false) Long groupeId,
            @RequestParam(value = "groupeIds", required = false) List<Long> groupeIds,
            @RequestParam(value = "requestId", required = false) Long requestId,
            @RequestParam(value = "lang", defaultValue = "en") String lang,
            @RequestParam(value = "profilePictureUrl", required = false) String profilePictureUrl
    ) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = cloudinaryService.uploadFile(image);
            } else if (profilePictureUrl != null && !profilePictureUrl.isBlank()) {
                imageUrl = profilePictureUrl;
            }

            User newUser = User.builder()
                    .fullName(fullname)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .profilePicture(imageUrl)
                    .role(role)
                    .specialite(specialite)
                    .build();

            if (role == Roles.STUDENT && groupeId != null) {
                var g = groupeRepository.findById(groupeId)
                        .orElseThrow(() -> new RuntimeException("Groupe not found"));
                if (g.getStudentCapacity() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No student capacity left in this group"));
                }
                newUser.setStudentGroupe(g);
                g.setStudentCapacity(g.getStudentCapacity() - 1);
                groupeRepository.save(g);
            } else if (role == Roles.TRAINER && groupeIds != null && !groupeIds.isEmpty()) {
                var groupes = groupeRepository.findAllById(groupeIds);
                for (var g : groupes) {
                    if (g.getTrainerCapacity() <= 0) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "No trainer capacity left in group " + g.getNom()));
                    }
                    g.setTrainerCapacity(g.getTrainerCapacity() - 1);
                    groupeRepository.save(g);
                }
                newUser.setTrainerGroupes(groupes);
            }

            userRepository.save(newUser);

            var locale = new Locale(lang);
            emailService.sendWelcomeEmail(
                    newUser.getEmail(),
                    newUser.getFullName(),
                    newUser.getEmail(),
                    password,
                    locale
            );

            if (requestId != null) {
                var req = requestRepository.findById(requestId)
                        .orElseThrow(() -> new RuntimeException("Request not found"));
                req.setStatus(RequestStatus.ACCEPTED);
                requestRepository.save(req);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully!",
                    "userEmail", newUser.getEmail(),
                    "role", newUser.getRole().name(),
                    "specialite", newUser.getSpecialite() != null ? newUser.getSpecialite() : "Not specified"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to register user: " + e.getMessage()));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("exists", userRepository.existsByEmail(email)));
    }

    @GetMapping("/students")
    public List<User> getStudentsBySpecialite(@RequestParam String specialite) {
        return userRepository.findByRoleAndSpecialite(Roles.STUDENT, specialite);
    }

    @GetMapping("/trainers")
    public List<User> getTrainersBySpecialite(@RequestParam String specialite) {
        return userRepository.findByRoleAndSpecialite(Roles.TRAINER, specialite);
    }

    @GetMapping("/trainers/by-groupe/{groupeId}")
    public ResponseEntity<List<User>> getTrainersByGroupe(@PathVariable Long groupeId) {
        return ResponseEntity.ok(authService.findTrainersByGroupe(groupeId));
    }
}
