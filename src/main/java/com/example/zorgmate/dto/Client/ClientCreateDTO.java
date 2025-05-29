package com.example.zorgmate.dto.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientCreateDTO {
    @NotBlank(message = "Naam mag niet leeg zijn")
    private String name;


    @NotBlank(message = "Email-adres is verplicht")
    private String email;


    @NotBlank(message = "Telefoonnummer is verplicht")
    private String phone;
}
