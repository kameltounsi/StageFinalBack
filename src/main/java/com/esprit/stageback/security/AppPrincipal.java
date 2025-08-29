package com.esprit.stageback.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class AppPrincipal implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final Long id;
    private final String email;
    private final String fullName;
    private final String role; // "ADMIN", "TRAINER", etc.

    public Collection<? extends GrantedAuthority> toAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String toString() {
        return "AppPrincipal{id=" + id + ", email='" + email + "'}";
    }
}
