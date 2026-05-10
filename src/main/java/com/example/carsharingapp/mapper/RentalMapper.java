package com.example.carsharingapp.mapper;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import com.example.carsharingapp.model.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    Rental toEntity(CreateRentalRequestDto dto);

    RentalResponseDto toDto(Rental rental);
}
