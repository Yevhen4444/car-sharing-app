package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import java.util.List;

public interface RentalService {
    RentalResponseDto createRental(CreateRentalRequestDto dto);

    RentalResponseDto returnRental(Long rentalId);

    RentalResponseDto getById(Long rentalId);

    List<RentalResponseDto> getRentals(Long userId, Boolean isActive);
}
