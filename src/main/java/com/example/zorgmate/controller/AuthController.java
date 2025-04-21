package com.example.zorgmate.controller;

import com.example.zorgmate.dto.User.UserLoginDTO;
import com.example.zorgmate.dal.entity.User.User;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.jwt.UserTokenDTO;
import com.example.zorgmate.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        UserTokenDTO dto = new UserTokenDTO(user.getId(), user.getUsername(), user.getRole().name());
        String token = jwtUtil.generateToken(dto);

        return ResponseEntity.ok().body(Map.of("token", token));
    }
}
