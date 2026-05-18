package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        rentalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void register_ValidRequest_ShouldReturnRegisteredUser() throws Exception {
        UserRegistrationRequestDto requestDto =
                TestDataHelper.createUserRegistrationRequestDto(
                        "test@mail.com",
                        "password123",
                        "password123"
                );

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
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        UserLoginRequestDto requestDto =
                TestDataHelper.createUserLoginRequestDto(
                        "test@mail.com",
                        "password123"
                );

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
