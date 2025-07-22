package com.esprit.stageback.services;


import com.esprit.stageback.dto.AuthResponse;
import com.esprit.stageback.dto.LoginRequest;
import com.esprit.stageback.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}
