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
    @Email(message = "Geen geldig email-adres")
    private String email;

    @NotBlank(message = "Telefoonnummer is verplicht")
    private String phone;

    @NotBlank(message = "Postcode is verplicht")
    private String postcode;

    @NotBlank(message = "Huisnummer is verplicht")
    private String houseNumber;

    @NotBlank(message = "Straat is verplicht")
    private String street;

    @NotBlank(message = "Stad is verplicht")
    private String city;
}
