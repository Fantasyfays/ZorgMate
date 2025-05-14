package com.example.zorgmate.dto.Client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
}
