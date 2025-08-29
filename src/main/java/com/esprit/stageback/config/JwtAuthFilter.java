package com.esprit.stageback.config;

import com.esprit.stageback.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // Spring Security UserDetails impl
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/",
            "/swagger-ui",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/swagger-resources",
            "/webjars",
            "/configuration"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) Laisser passer Swagger + OPTIONS
        if (isPreflight(request) || isSwaggerPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Ne traiter le JWT que si un header Bearer est présent
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String userEmail = jwtService.extractEmail(jwt); // sub = email
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userEntity = userRepository.findByEmail(userEmail).orElse(null);
                if (userEntity != null && jwtService.isTokenValid(jwt, userEntity)) {

                    // ✅ principal = UserDetails avec username = email
                    var userDetails = User.withUsername(userEntity.getEmail())
                            .password(userEntity.getPassword() != null ? userEntity.getPassword() : "")
                            .authorities(userEntity.getAuthorities()) // ou List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(false)
                            .build();

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            writeJsonError(response, 401, "Session expirée. Veuillez vous reconnecter.");
            return; // ✅ stopper la chaîne après avoir écrit la réponse
        } catch (SignatureException | MalformedJwtException ex) {
            writeJsonError(response, 401, "Token invalide.");
            return; // ✅ stopper la chaîne après avoir écrit la réponse
        }
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isSwaggerPath(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String p : SWAGGER_WHITELIST) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    private void writeJsonError(HttpServletResponse res, int status, String message) throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
