package com.example.zorgmate.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserTokenDTO {
    private Long userId;
    private String username;
    private String role;
}
