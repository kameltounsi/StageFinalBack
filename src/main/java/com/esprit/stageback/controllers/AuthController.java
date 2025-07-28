package com.esprit.stageback.controllers;



import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.AuthService;
import com.esprit.stageback.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.esprit.stageback.config.JwtService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthService authService, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authService = authService;
        this.cloudinaryService = cloudinaryService;
    }
    private final UserRepository userRepository; // ✅ Ajouté
    private final PasswordEncoder passwordEncoder; // ✅ Ajouté
    private final JwtService jwtService;
    private final AuthService authService;
    private final CloudinaryService cloudinaryService;
    /* @PostMapping("/register")
     public ResponseEntity<?> registerUser(@RequestParam("fullname") String fullname,
                                           @RequestParam("email") String email,
                                           @RequestParam("password") String password,
                                           @RequestParam("image") MultipartFile image) {
         // ⬆️ Uploader sur Cloudinary
         String imageUrl = cloudinaryService.uploadFile(image);

         // 🛠️ Construction de l'utilisateur
         User newUser = User.builder()
                 .fullName(fullname)
                 .email(email)
                 .password(passwordEncoder.encode(password))
                 .profilePicture(imageUrl)
                 .role(Roles.USER)
                 .build();

         // 💾 Enregistrement
         userRepository.save(newUser);

         return ResponseEntity.ok("User registered successfully");
     }*/
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
    /*
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Roles.valueOf(role.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok("Role updated successfully");
    }*/

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

    @PostMapping("/add-user")
    public ResponseEntity<Map<String, String>> addUser(
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("image") MultipartFile image,
            @RequestParam("role") Roles role) {

        Map<String, String> response = new HashMap<>();

        try {
            // Vérifier si l'email existe déjà
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("error", "Email already exists.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // 📤 Upload de l'image sur Cloudinary
            String imageUrl;
            try {
                imageUrl = cloudinaryService.uploadFile(image);
            } catch (Exception e) {
                response.put("error", "Image upload failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Vérifier mot de passe minimal (par ex. 8 caractères)
            if (password.length() < 8) {
                response.put("error", "Password must be at least 8 characters long.");
                return ResponseEntity.badRequest().body(response);
            }

            // 🛠️ Construction du nouvel utilisateur
            User newUser = User.builder()
                    .fullName(fullname)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .profilePicture(imageUrl)
                    .role(role)
                    .build();

            // 💾 Sauvegarde en base de données
            userRepository.save(newUser);

            // ✅ Réponse succès
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // ⚠️ Erreur inattendue
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
