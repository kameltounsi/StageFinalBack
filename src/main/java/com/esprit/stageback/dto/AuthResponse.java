// src/main/java/com/esprit/stageback/dto/AuthResponse.java
package com.esprit.stageback.dto;

import com.esprit.stageback.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private User user;
}
