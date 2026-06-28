package com.example.carsharingapp.mapper;

import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.UserRegistrationResponseDto;
import com.example.carsharingapp.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegistrationRequestDto dto);

    UserRegistrationResponseDto toDto(User user);
}
