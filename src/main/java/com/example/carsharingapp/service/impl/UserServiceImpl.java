package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserLoginResponseDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.UserRegistrationResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.mapper.UserMapper;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.security.JwtUtil;
import com.example.carsharingapp.service.UserService;
import com.example.carsharingapp.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto dto) {
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            throw new RegistrationException("Passwords do not match");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RegistrationException("Email already exists");
        }
        User user = userMapper.toEntity(dto);
        user.setRole(Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Email not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RegistrationException("Invalid password");
        }

        UserLoginResponseDto responseDto = new UserLoginResponseDto();
        responseDto.setToken(jwtUtil.generateToken(user.getEmail()));
        return responseDto;
    }
}
