package com.example.zorgmate.controller;

import com.example.zorgmate.dal.entity.User.User;
import com.example.zorgmate.dal.entity.User.UserRole;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User existingUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        existingUser = new User();
        existingUser.setUsername("user");
        existingUser.setPassword("123");
        existingUser.setRole(UserRole.USER);
        userRepository.save(existingUser);
    }

    @Test
    @WithMockUser
    public void registerUser_ValidRequest_Returns200AndUserDTO() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole(UserRole.USER);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser
    public void registerUser_UsernameAlreadyExists_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("123");
        request.setRole(UserRole.USER);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Gebruikersnaam word al gebruikt"));
    }

    @Test
    @WithMockUser
    public void getUserById_ExistingUser_ReturnsUserDTO() throws Exception {
        mockMvc.perform(get("/api/users/" + existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    @WithMockUser
    public void getUserById_NonExistingUser_Returns404() throws Exception {
        mockMvc.perform(get("/api/users/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Gebruiker met ID 9 niet gevonden"));
    }


    @Test
    @WithMockUser
    public void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updateduser");
        request.setRole(UserRole.ADMIN);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser
    public void updateUser_NonExistingUser_Returns404() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updateduser");
        request.setRole(UserRole.ADMIN);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/users/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Gebruiker met ID 999999 niet gevonden")); // optioneel nauwkeuriger
    }


    @Test
    @WithMockUser
    public void updateUser_InvalidRequest_Returns400WithValidationErrors() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("");
        request.setRole(null);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(hasKey("username")));
    }

    @Test
    @WithMockUser
    public void deleteUser_ExistingUser_Returns200() throws Exception {
        mockMvc.perform(delete("/api/users/" + existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User with ID " + existingUser.getId() + " has been deleted."));

        Optional<User> deleted = userRepository.findById(existingUser.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @WithMockUser
    public void deleteUser_NonExistingUser_Returns404() throws Exception {
        mockMvc.perform(delete("/api/users/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Gebruiker met ID 999999 niet gevonden")); // optioneel nauwkeuriger
    }

}
