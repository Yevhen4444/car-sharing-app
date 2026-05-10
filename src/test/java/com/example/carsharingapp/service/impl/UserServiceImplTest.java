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
import com.example.carsharingapp.util.TestDataHelper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loginShouldThrowRegistrationExceptionWhenPasswordIsInvalid() {
        UserLoginRequestDto dto = TestDataHelper.createUserLoginRequestDto(
                "test@mail.com",
                "wrong-password");
        User user = TestDataHelper.createUser(
                "test@mail.com",
                "encoded-password");
        when(userRepository.findByEmail(dto.getEmail()))
        .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
        .thenReturn(false);
        assertThrows(RegistrationException.class,
                () -> userService.login(dto));
        verify(userRepository, times(1))
                .findByEmail(dto.getEmail());
        verify(passwordEncoder, times(1))
                .matches(dto.getPassword(), user.getPassword());
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        UserLoginRequestDto dto = TestDataHelper.createUserLoginRequestDto(
                "test@mail.com",
                "password");
        User user = TestDataHelper.createUser(
                "test@mail.com",
                "encoded-password");
        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail()))
                .thenReturn("test-token");
        UserLoginResponseDto actual = userService.login(dto);
        assertEquals("test-token", actual.getToken());
        verify(userRepository, times(1))
        .findByEmail(dto.getEmail());
        verify(passwordEncoder, times(1))
                .matches(dto.getPassword(), user.getPassword());
        verify(jwtUtil, times(1))
                .generateToken(user.getEmail());
    }

    @Test
    void registerShouldThrowRegistrationExceptionWhenPasswordsDoNotMatch() {
       UserRegistrationRequestDto dto = TestDataHelper.createUserRegistrationRequestDto(
               "test@mail.com",
               "password",
               "repeat-password");
       assertThrows(RegistrationException.class,
               () -> userService.register(dto));
        verify(userRepository, times(0))
                .existsByEmail(any());
    }

    @Test
    void registerShouldRegisterUserSuccessfully() {
        UserRegistrationRequestDto dto = TestDataHelper.createUserRegistrationRequestDto(
                "test@mail.com",
                "password",
                "password");
        User user = TestDataHelper.createUser(
                "test@mail.com",
                "encoded-password");
        User savedUser = TestDataHelper.createUser(
                "test@mail.com",
                "encoded-password");
        when(userRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);
        when(userMapper.toEntity(dto))
                .thenReturn(user);
        when(passwordEncoder.encode(dto.getPassword()))
        .thenReturn("encoded-password");
        when(userRepository.save(user))
                .thenReturn(savedUser);
        UserRegistrationResponseDto responseDto = TestDataHelper.createUserRegistrationResponseDto(
                1L,
                "test@mail.com",
                "firstName",
                "lastName");
        when(userMapper.toDto(savedUser))
                .thenReturn(responseDto);
        UserRegistrationResponseDto actual = userService.register(dto);
        assertEquals(responseDto, actual);
        assertEquals(Role.CUSTOMER, user.getRole());
        assertEquals("encoded-password", user.getPassword());
        verify(userRepository, times(1))
                .existsByEmail(dto.getEmail());
        verify(userMapper, times(1))
                .toEntity(dto);
        verify(passwordEncoder, times(1))
                .encode(dto.getPassword());
        verify(userRepository, times(1))
                .save(user);
        verify(userMapper, times(1))
                .toDto(savedUser);
    }
}
