package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerUser(RegisterRequest request);
    void deleteUser(Long userId);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long userId);
    UserDTO updateUser(Long userId, RegisterRequest request);
    UserDTO getUserByUsername(String username);
}
