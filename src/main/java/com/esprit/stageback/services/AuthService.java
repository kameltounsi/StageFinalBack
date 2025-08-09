package com.esprit.stageback.services;


import com.esprit.stageback.dto.AuthResponse;
import com.esprit.stageback.dto.LoginRequest;
import com.esprit.stageback.dto.RegisterRequest;
import com.esprit.stageback.entities.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<User> findTrainersByGroupe(Long groupeId);

}
