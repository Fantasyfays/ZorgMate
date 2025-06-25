package com.example.zorgmate.service;

import com.example.zorgmate.dal.entity.User.User;
import com.example.zorgmate.dal.entity.User.UserRole;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UpdateUserRequest;
import com.example.zorgmate.dto.User.UserDTO;
import com.example.zorgmate.exception.UserNotFoundException;
import com.example.zorgmate.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("fays", "123", UserRole.USER);
        when(userRepository.findByUsername("fays")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Gebruikersnaam word al gebruikt", exception.getMessage());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        Long userId = 5L;
        UpdateUserRequest request = new UpdateUserRequest("nieuwefays", UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userId, request);
        });

        assertEquals("Gebruiker met ID 5 niet gevonden", exception.getMessage());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {
        Long userId = 5L;

        when(userRepository.existsById(userId)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("Gebruiker met ID 5 niet gevonden", exception.getMessage());
    }

    @Test
    void registerUser_ShouldSaveAndReturnUserDTO_WhenValid() {
        RegisterRequest request = new RegisterRequest("omar", "1234", null);
        User savedUser = new User(1L, "omar", "beveiligd", UserRole.USER);

        when(userRepository.findByUsername("omar")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("geheim")).thenReturn("beveiligd");
        when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.registerUser(request);

        assertEquals("omar", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());
        assertEquals(1L, result.getId());
    }
}
