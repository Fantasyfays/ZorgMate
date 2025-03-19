package com.example.zorgmate.Service.interfaces;

import com.example.zorgmate.dal.entity.User;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UserDTO;

import java.util.List;

public interface UserService {
    User registerUser(RegisterRequest request);
    void deleteUser(Long userId);
    List<UserDTO> getAllUsers();
}
