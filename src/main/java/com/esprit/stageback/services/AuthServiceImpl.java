package com.esprit.stageback.services;
import com.esprit.stageback.dto.*;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.Status;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.STUDENT) // default role
                .build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .user(user)
                .build();    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // ✅ Met à jour le statut de l'utilisateur
        user.setStatus(Status.ONLINE);
        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .user(user)
                .build();
    }
    public ResponseEntity<?> logout(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(Status.NOT_VISIBLE);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
    /*
    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> verificationCodes = new HashMap<>();

    public ResponseEntity<?> sendVerificationCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        String code = String.format("%04d", new Random().nextInt(10000));
        verificationCodes.put(email, code);

        // send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your verification code");
        message.setText("Your password reset code is: " + code);
        mailSender.send(message);

        return ResponseEntity.ok("Verification code sent");
    }
*/
}
