package com.esprit.stageback.controllers;

import com.esprit.stageback.dto.ChangePasswordRequest;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(Authentication auth,
                                                            @RequestParam("file") MultipartFile file) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String url = cloudinaryService.uploadAvatar(file, user.getId());
        user.setProfilePicture(url);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(Authentication auth,
                                            @RequestBody ChangePasswordRequest req) {
        if (req == null || req.getCurrentPassword() == null || req.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Both currentPassword and newPassword are required."));
        }
        if (req.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 8 characters."));
        }

        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect."));
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);

        return ResponseEntity.noContent().build();
    }
}
