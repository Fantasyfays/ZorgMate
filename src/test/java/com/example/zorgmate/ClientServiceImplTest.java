package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dto.Client.ClientCreateDTO;
import com.example.zorgmate.dto.Client.ClientResponseDTO;
import com.example.zorgmate.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepo;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createClient_shouldReturnClientDTO_whenValidDataProvided() {
        // Arrange
        String username = "testuser";

        ClientCreateDTO dto = ClientCreateDTO.builder()
                .name("NewClient")
                .email("client@example.com")
                .phone("0123456789")
                .build();

        Client savedClient = Client.builder()
                .id(1L)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .createdBy(username)
                .build();

        when(clientRepo.save(any())).thenReturn(savedClient);

        // Act
        ClientResponseDTO result = clientService.createClient(dto, username);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("NewClient", result.getName());
        assertEquals("client@example.com", result.getEmail());
        assertEquals("0123456789", result.getPhone());
    }

    @Test
    void createClient_shouldSaveCorrectEntityWithUsername() {
        // Arrange
        String username = "testuser";

        ClientCreateDTO dto = ClientCreateDTO.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .phone("9876543210")
                .build();

        when(clientRepo.save(any())).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(42L);
            return client;
        });

        // Act
        clientService.createClient(dto, username);

        // Assert
        verify(clientRepo).save(clientCaptor.capture());
        Client captured = clientCaptor.getValue();
        assertEquals("Jane Doe", captured.getName());
        assertEquals("jane@example.com", captured.getEmail());
        assertEquals("9876543210", captured.getPhone());
        assertEquals("testuser", captured.getCreatedBy());
    }

    @Test
    void getAllClientsForUser_shouldReturnClientList_whenDataExists() {
        // Arrange
        String username = "testuser";
        Client client = Client.builder()
                .id(1L)
                .name("ZorgClient")
                .email("zorg@example.com")
                .phone("12345")
                .createdBy(username)
                .build();

        when(clientRepo.findByCreatedBy(username)).thenReturn(List.of(client));

        // Act
        List<ClientResponseDTO> result = clientService.getAllClientsForUser(username);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ZorgClient", result.get(0).getName());
    }

    @Test
    void getAllClientsForUser_shouldReturnEmptyList_whenNoClientsExist() {
        // Arrange
        String username = "emptyuser";
        when(clientRepo.findByCreatedBy(username)).thenReturn(Collections.emptyList());

        // Act
        List<ClientResponseDTO> result = clientService.getAllClientsForUser(username);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
