package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserLoginResponseDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.UserRegistrationResponseDto;
import com.example.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @Operation(summary = "Register a new user")
    @PostMapping("/registration")
    public UserRegistrationResponseDto register(@Valid @RequestBody UserRegistrationRequestDto dto) {
        return userService.register(dto);
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto dto) {
        return userService.login(dto);
    }
}
