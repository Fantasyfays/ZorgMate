package com.example.zorgmate.Service.interfaces;

import com.example.zorgmate.dal.entity.User;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(RegisterRequest request);
    Optional<UserDTO> getUserByEmail(String email);
    List<UserDTO> getAllUsers();
}