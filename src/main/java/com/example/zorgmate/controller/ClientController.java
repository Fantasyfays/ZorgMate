package com.example.zorgmate.controller;

import com.example.zorgmate.dto.Client.ClientCreateDTO;
import com.example.zorgmate.dto.Client.ClientResponseDTO;
import com.example.zorgmate.service.interfaces.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ClientResponseDTO createClient(@Valid @RequestBody ClientCreateDTO dto, Authentication authentication) {
        String username = authentication.getName();
        return clientService.createClient(dto, username);
    }

    @GetMapping
    public List<ClientResponseDTO> getAllClients(Authentication authentication) {
        String username = authentication.getName();
        return clientService.getAllClientsForUser(username);
    }
}
