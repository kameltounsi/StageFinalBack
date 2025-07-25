package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.ForgotPasswordRequest;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.entities.VerificationStatus;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
/*
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        System.out.println("📩 Email reçu : '" + email + "'");
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("addresse email "+email);
        String code = String.format("%04d", new Random().nextInt(10000));
        user.setResetCode(code);
        user.setVerificationStatus(VerificationStatus.PENDING);
        userRepository.save(user);

        emailService.sendResetCode(email, code);

        return ResponseEntity.ok("Code sent");
    }
*/
@PostMapping("/forgot-password")
public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
    String email = request.getEmail();
    System.out.println("📩 Email reçu : '" + email + "'");

    User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String code = String.format("%04d", new Random().nextInt(10000));
    user.setResetCode(code);
    user.setVerificationStatus(VerificationStatus.PENDING);
    userRepository.save(user);

    emailService.sendResetCode(email, code);

    return ResponseEntity.ok("Code sent");
}

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getResetCode().equals(code)) {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
            userRepository.save(user);
            return ResponseEntity.ok("Code correct");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code incorrect");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Code non vérifié");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setVerificationStatus(VerificationStatus.PENDING);
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès");
    }
    @GetMapping("/send-test")
    public ResponseEntity<String> sendTest() {
        try {
            emailService.sendTestEmail();
            return ResponseEntity.ok("✅ Email envoyé !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Erreur : " + e.getMessage());
        }
    }
}
