package com.esprit.stageback.controllers;



import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.*;
import com.esprit.stageback.repositories.GroupeRepository;
import com.esprit.stageback.repositories.StudentRequestRepository;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AuthService;
import com.esprit.stageback.services.CloudinaryService;
import com.esprit.stageback.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.esprit.stageback.config.JwtService;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.esprit.stageback.entities.RequestStatus.ACCEPTED;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    public AuthController(UserRepository userRepository, GroupeRepository groupeRepository, StudentRequestRepository requestRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthService authService, CloudinaryService cloudinaryService, EmailService emailService) {
        this.userRepository = userRepository;
        this.groupeRepository = groupeRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authService = authService;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
    }
    private final UserRepository userRepository;
    private final GroupeRepository groupeRepository;
    private final StudentRequestRepository requestRepository;
    // ✅ Ajouté
    private final PasswordEncoder passwordEncoder; // ✅ Ajouté
    private final JwtService jwtService;
    private final AuthService authService;
    private final CloudinaryService cloudinaryService;
    private  final EmailService emailService;
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("image") MultipartFile image) {

        // 📤 Upload de l'image sur Cloudinary
        String imageUrl = cloudinaryService.uploadFile(image);

        // 🛠️ Construction du nouvel utilisateur
        User newUser = User.builder()
                .fullName(fullname)
                .email(email)
                .password(passwordEncoder.encode(password))
                .profilePicture(imageUrl)
                .role(Roles.STUDENT)
                .build();

        // 💾 Sauvegarde en base de données
        userRepository.save(newUser);

        // 🔁 Réponse propre
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }




    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAuthentication(@RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            System.out.println("Token received: " + token); // Debug log
            String email = jwtService.extractEmail(token);
            System.out.println("Extracted email: " + email); // Debug log
            return ResponseEntity.ok(email != null);
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage()); // Debug log
            return ResponseEntity.ok(false);
        }
    }
   // @PreAuthorize("hasRole('ADMIN')")
   @PreAuthorize("permitAll()")

   @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }


    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Roles.valueOf(role.toUpperCase()));
        userRepository.save(user);

        // ✅ Return JSON response instead of raw String
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated successfully");
        return ResponseEntity.ok(response);
    }
/*
@PostMapping("/add-user")
public ResponseEntity<Map<String, String>> addUser(
        @RequestParam("fullname") String fullname,
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("image") MultipartFile image,
        @RequestParam("role") Roles role,
        @RequestParam(value = "lang", defaultValue = "en") String lang) {

    // Upload de l'image vers Cloudinary
    String imageUrl = cloudinaryService.uploadFile(image);

    // Création du nouvel utilisateur
    User newUser = User.builder()
            .fullName(fullname)
            .email(email)
            .password(passwordEncoder.encode(password))
            .profilePicture(imageUrl)
            .role(role)
            .build();

    userRepository.save(newUser);

    // Déterminer la langue à partir du paramètre reçu
    Locale locale = new Locale(lang);

    // Envoi de l’email avec i18n
    emailService.sendWelcomeEmail(email, fullname, email, password, locale);

    // Réponse
    Map<String, String> response = new HashMap<>();
    response.put("message", "User registered successfully & welcome email sent!");
    return ResponseEntity.ok(response);
}
*/
@PostMapping("/add-user")
public ResponseEntity<Map<String, String>> addUser(
        @RequestParam("fullname") String fullname,
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam(value = "image", required = false) MultipartFile image,
        @RequestParam("role") Roles role,
        @RequestParam(value = "specialite", required = false) String specialite, // 🔹 ajouté
        @RequestParam(value = "groupeId", required = false) Long groupeId, // pour STUDENT
        @RequestParam(value = "groupeIds", required = false) List<Long> groupeIds, // pour TRAINER
        @RequestParam(value = "requestId", required = false) Long requestId,
        @RequestParam(value = "lang", defaultValue = "en") String lang,
        @RequestParam(value = "profilePictureUrl", required = false) String profilePictureUrl
) {
    try {
        // 🔹 1. Gestion de l’image
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(image);
        } else if (profilePictureUrl != null && !profilePictureUrl.isBlank()) {
            imageUrl = profilePictureUrl;
        }

        // 🔹 2. Création du nouvel utilisateur
        User newUser = User.builder()
                .fullName(fullname)
                .email(email)
                .password(passwordEncoder.encode(password))
                .profilePicture(imageUrl)
                .role(role)
                .specialite(specialite) // 🔹 on enregistre la spécialité
                .build();

        // 🔹 3. Affectation selon le rôle
        if (role == Roles.STUDENT && groupeId != null) {
            Groupe groupe = groupeRepository.findById(groupeId)
                    .orElseThrow(() -> new RuntimeException("Groupe not found"));
            newUser.setStudentGroupe(groupe);
        } else if (role == Roles.TRAINER && groupeIds != null && !groupeIds.isEmpty()) {
            List<Groupe> groupes = groupeRepository.findAllById(groupeIds);
            newUser.setTrainerGroupes(groupes);
        }

        // 🔹 4. Sauvegarde
        userRepository.save(newUser);

        // 🔹 5. Email de bienvenue
        Locale locale = new Locale(lang);
        emailService.sendWelcomeEmail(
                newUser.getEmail(),
                newUser.getFullName(),
                newUser.getEmail(),
                password,
                locale
        );

        // 🔹 6. Mise à jour de la requête si elle existe
        if (requestId != null) {
            StudentRequest req = requestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            req.setStatus(RequestStatus.ACCEPTED);
            requestRepository.save(req);
        }

        // 🔹 7. Réponse
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        response.put("userEmail", newUser.getEmail());
        response.put("role", newUser.getRole().name());
        response.put("specialite", newUser.getSpecialite() != null ? newUser.getSpecialite() : "Not specified");

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Failed to register user: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}



    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/students")
    public List<User> getStudentsBySpecialite(@RequestParam String specialite) {
        return userRepository.findByRoleAndSpecialite(Roles.STUDENT, specialite);
    }

    @GetMapping("/trainers")
    public List<User> getTrainersBySpecialite(@RequestParam String specialite) {
        return userRepository.findByRoleAndSpecialite(Roles.TRAINER, specialite);
    }
}
