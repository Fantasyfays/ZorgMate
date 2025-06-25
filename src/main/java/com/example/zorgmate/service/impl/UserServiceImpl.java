package com.example.zorgmate.service.impl;

import com.example.zorgmate.exception.UserNotFoundException;
import com.example.zorgmate.service.interfaces.UserService;
import com.example.zorgmate.dal.entity.User.User;
import com.example.zorgmate.dal.entity.User.UserRole;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UpdateUserRequest;
import com.example.zorgmate.dto.User.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Gebruikersnaam word al gebruikt");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.USER;

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .orElseThrow(() -> new UserNotFoundException("Gebruiker met ID " + userId + " niet gevonden"));
    }

    @Override
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Gebruiker met ID " + userId + " niet gevonden"));

        user.setUsername(request.getUsername());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        return new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRole());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Gebruiker met ID " + userId + " niet gevonden");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .orElseThrow(() -> new UserNotFoundException("Gebruiker met gebruikersnaam '" + username + "' niet gevonden"));
    }
}
