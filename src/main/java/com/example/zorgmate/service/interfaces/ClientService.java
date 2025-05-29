package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dto.Client.ClientCreateDTO;
import com.example.zorgmate.dto.Client.ClientResponseDTO;

import java.util.List;

public interface ClientService {
    ClientResponseDTO createClient(ClientCreateDTO dto, String username);
    List<ClientResponseDTO> getAllClientsForUser(String username);
}
