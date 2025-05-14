package com.example.zorgmate.service.impl;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dto.Client.ClientCreateDTO;
import com.example.zorgmate.dto.Client.ClientResponseDTO;
import com.example.zorgmate.service.interfaces.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ClientResponseDTO createClient(ClientCreateDTO dto) {
        Client client = Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Override
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ClientResponseDTO mapToDTO(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .build();
    }
}
