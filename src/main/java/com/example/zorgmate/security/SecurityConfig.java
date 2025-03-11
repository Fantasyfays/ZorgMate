package com.example.zorgmate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configureer CORS
                .csrf(csrf -> csrf.disable()) // Schakel CSRF-bescherming uit
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessies
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Sta openbare toegang toe tot Swagger
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll() // Openbare toegang tot authenticatie endpoints
                        .requestMatchers("/api/auth/me", "/api/invoices/**").authenticated() // Vereis authenticatie voor deze endpoints
                        .anyRequest().permitAll() // Sta toegang toe voor alle andere endpoints
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Voeg de JWT-filter toe
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Sta framing toe voor H2-console
                );

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
        source.registerCorsConfiguration("/**", corsConfiguration); // Pas toe op alle endpoints
        return source;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Definieer de BCryptPasswordEncoder bean
    }
}
