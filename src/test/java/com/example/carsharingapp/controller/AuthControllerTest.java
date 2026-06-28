package com.example.carsharingapp.controller;

import com.example.carsharingapp.config.MySqlTestContainer;
import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest extends MySqlTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void register_ValidRequest_ShouldReturnRegisteredUser() throws Exception {
        UserRegistrationRequestDto requestDto =
                TestDataHelper.createUserRegistrationRequestDto(
                        "test1@mail.com",
                        "password123",
                        "password123");

        mockMvc.perform(post("/auth/registration")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value(requestDto.getEmail()));

        boolean exists = userRepository
                .findByEmail(requestDto.getEmail())
                .isPresent();

        assertTrue(exists);
    }

    @Test
    @WithMockUser
    void login_ValidRequest_ShouldReturnToken() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("login@mail.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        UserLoginRequestDto requestDto =
                TestDataHelper.createUserLoginRequestDto(
                        "login@mail.com",
                        "password123");

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_InvalidPassword_ShouldReturnUnauthorized() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("test@mail.com");
        user.setPassword(passwordEncoder.encode("correct"));
        userRepository.save(user);

        UserLoginRequestDto dto =
                TestDataHelper.createUserLoginRequestDto("test@mail.com", "wrong");

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void register_DuplicateEmail_ShouldReturnBadRequest() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("dup@mail.com");
        user.setPassword(passwordEncoder.encode("123"));
        userRepository.save(user);

        UserRegistrationRequestDto dto =
                TestDataHelper.createUserRegistrationRequestDto(
                        "dup@mail.com", "password", "password");

        mockMvc.perform(post("/auth/registration")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void getById_InvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/rentals/999999"))
                .andExpect(status().isNotFound());
    }
}
