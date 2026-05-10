package com.example.carsharingapp.dto;

import com.example.carsharingapp.model.Role;
import lombok.Data;

@Data
public class UserRegistrationResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
