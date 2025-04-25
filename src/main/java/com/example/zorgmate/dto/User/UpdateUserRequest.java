package com.example.zorgmate.dto.User;

import com.example.zorgmate.dal.entity.User.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Username mag niet leeg zijn")
    private String username;

    private UserRole role;
}
