package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserLoginResponseDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.UserRegistrationResponseDto;

public interface UserService {

    UserRegistrationResponseDto register(UserRegistrationRequestDto dto);

    UserLoginResponseDto login(UserLoginRequestDto dto);
}
