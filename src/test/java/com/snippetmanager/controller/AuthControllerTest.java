package com.snippetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetmanager.model.User;
import com.snippetmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User sampleUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        sampleUser = new User("Abhishek Kumar Singh", "abhishek.singh", "pass99", "Member");
        sampleUser.setId(1L);
    }

    @Test
    @DisplayName("POST /api/auth/signup - Register new user account")
    void shouldSignupNewUser() throws Exception {
        when(userRepository.findByUsernameIgnoreCase("abhishek.singh")).thenReturn(Optional.empty());
        when(userRepository.findByNameIgnoreCase("Abhishek Kumar Singh")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Abhishek Kumar Singh",
                                "username", "abhishek.singh",
                                "password", "pass99"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("abhishek.singh"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Sign in with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        when(userRepository.findByUsernameIgnoreCase("abhishek.singh")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "identifier", "abhishek.singh",
                                "password", "pass99"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Abhishek Kumar Singh"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Reject wrong password with 401")
    void shouldRejectInvalidPassword() throws Exception {
        when(userRepository.findByUsernameIgnoreCase("abhishek.singh")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "identifier", "abhishek.singh",
                                "password", "wrongpass"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/auth/change-password - Update user password")
    void shouldChangePassword() throws Exception {
        when(userRepository.findByUsernameIgnoreCase("abhishek.singh")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "abhishek.singh",
                                "currentPassword", "pass99",
                                "newPassword", "newpass123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }
}
