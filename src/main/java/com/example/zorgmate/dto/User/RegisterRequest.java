package com.example.zorgmate.dto.User;

import com.example.zorgmate.dal.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username mag niet leeg zijn")
    private String username;

    @NotBlank(message = "Email mag niet leeg zijn")
    @Email(message = "Ongeldig e-mailadres")
    private String email;

    private UserRole role;
}
