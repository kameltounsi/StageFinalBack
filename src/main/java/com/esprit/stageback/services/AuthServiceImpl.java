package com.esprit.stageback.services;

import com.esprit.stageback.dto.AuthResponse;
import com.esprit.stageback.dto.LoginRequest;
import com.esprit.stageback.dto.RegisterRequest;
import com.esprit.stageback.dto.UserDTO;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.Status;
import com.esprit.stageback.entities.User;
import com.esprit.stageback.repositories.UserRepository;
import com.esprit.stageback.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.STUDENT)
                .build();
        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .user(toDTO(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        user.setStatus(Status.ONLINE);
        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .user(toDTOWithGroupIds(user))
                .build();
    }

    private UserDTO toDTO(User u) {
        String studentClass = (u.getRole() == Roles.STUDENT && u.getStudentGroupe() != null)
                ? u.getStudentGroupe().getNom()
                : "No class assigned";

        List<String> trainerClasses = (u.getRole() == Roles.TRAINER && u.getTrainerGroupes() != null)
                ? u.getTrainerGroupes().stream().map(Groupe::getNom).toList()
                : List.of();

        return UserDTO.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .profilePicture(u.getProfilePicture())
                .studentClass(studentClass)
                .studentGroupId(null)          // rempli dans la variante ci-dessous
                .trainerClasses(trainerClasses)
                .trainerGroupIds(List.of())    // rempli dans la variante ci-dessous
                .build();
    }

    // Variante qui renseigne aussi les IDs (utilisée au login)
    private UserDTO toDTOWithGroupIds(User u) {
        UserDTO dto = toDTO(u);

        Long studentGroupId = userRepository.findGroupIdByEmailIgnoreCase(u.getEmail()).orElse(null);
        dto.setStudentGroupId(studentGroupId);

        List<Long> trainerGroupIds = (u.getRole() == Roles.TRAINER)
                ? userRepository.findTrainerGroupIdsByEmailIgnoreCase(u.getEmail())
                : List.of();
        dto.setTrainerGroupIds(trainerGroupIds);

        return dto;
    }

    @Override
    public ResponseEntity<?> logout(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(Status.NOT_VISIBLE);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @Override
    public List<User> findTrainersByGroupe(Long groupeId) {
        return userRepository.findTrainersByGroupeId(groupeId);
    }
    // Ex-méthode private -> rendre publique pour la réutiliser dans /me
    public UserDTO publicToDTOWithGroupIds(User u) {
        UserDTO dto = toDTO(u);

        Long studentGroupId = userRepository.findGroupIdByEmailIgnoreCase(u.getEmail()).orElse(null);
        dto.setStudentGroupId(studentGroupId);

        List<Long> trainerGroupIds = (u.getRole() == Roles.TRAINER)
                ? userRepository.findTrainerGroupIdsByEmailIgnoreCase(u.getEmail())
                : List.of();
        dto.setTrainerGroupIds(trainerGroupIds);

        return dto;
    }

}
