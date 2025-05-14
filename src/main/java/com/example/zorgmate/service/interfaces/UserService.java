package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UpdateUserRequest;
import com.example.zorgmate.dto.User.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO registerUser(RegisterRequest request);

    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long userId);

    UserDTO updateUser(Long userId, UpdateUserRequest request); // ✅ aangepast

    void deleteUser(Long userId);

    UserDTO getUserByUsername(String username);
}
