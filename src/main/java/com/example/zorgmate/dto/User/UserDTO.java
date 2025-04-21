package com.example.zorgmate.dto.User;

import com.example.zorgmate.dal.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private UserRole role;
}
