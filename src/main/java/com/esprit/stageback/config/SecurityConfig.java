package com.esprit.stageback.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**", "/webjars/**", "/configuration/**"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final ApplicationConfig appConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"Authentification requise.\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"Accès refusé : droits insuffisants.\"}");
                        })
                )

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger public
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // --- Public (tes endpoints ouverts) ---
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/groups/**",
                                "/api/auth/students",
                                "/api/auth/trainers",
                                "/api/specialities/**",   // garde l'orthographe que tu utilises vraiment
                                "/api/plannings/**",
                                "/api/requests/**",
                                "/favicon.ico", "/", "/index.html",
                                "/emploi-temps/planning-pdf/**"
                        ).permitAll()

                        // Endpoints PDF publics spécifiques (si voulu)
                        .requestMatchers(HttpMethod.GET, "/api/plannings/groupe/*/pdf").permitAll()

                        // --- Zones protégées par rôle ---
                        // Emploi du temps "me" (JSON)
                        .requestMatchers("/api/trainers/me/**").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers("/api/students/me/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/trainers/me/attendance/**").hasAnyRole("TRAINER", "ADMIN")

                        // Emploi du temps "me" (PDF) — aligne bien le chemin côté Front : /api/trainers/me/weekly-schedule.pdf
                        .requestMatchers("/api/trainers/me/weekly-schedule.pdf").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers("/api/students/me/weekly-schedule.pdf").hasAnyRole("STUDENT", "ADMIN")
                        // Toute autre requête nécessite une auth
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appConfig.userDetailsService());
        provider.setPasswordEncoder(appConfig.passwordEncoder());
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Front Angular en dev
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // Expose pour récupération du nom de fichier, etc.
        configuration.setExposedHeaders(List.of(
                "Content-Disposition",
                "Authorization",
                "Content-Type",
                "Content-Length"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
