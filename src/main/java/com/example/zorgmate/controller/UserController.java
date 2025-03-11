package com.example.zorgmate.controller;

import com.example.zorgmate.Service.interfaces.UserService;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(new UserDTO(
                userService.registerUser(request).getId(),
                request.getUsername(),
                request.getEmail(),
                request.getRole()
        ));
    }

    @GetMapping("/{email}")
    public ResponseEntity<Optional<UserDTO>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}