package com.example.zorgmate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Schakel CSRF uit (handig voor een REST API zonder sessies)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Gebruik stateless sessies
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ✅ ALLE endpoints zijn openbaar en toegankelijk
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Sta framing toe voor H2-console
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // CORS instellingen

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:5173")); // Toegestane frontend-origin
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Toegestane HTTP-methoden
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept")); // Toegestane headers
        corsConfiguration.setExposedHeaders(List.of("Authorization")); // Expose headers indien nodig
        corsConfiguration.setAllowCredentials(true); // Sta cookies/credentials toe

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
