package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto create(CreateRentalRequestDto dto);

    RentalResponseDto returnRental(Long rentalId);

    RentalResponseDto getById(Long rentalId);

    Page<RentalResponseDto> getAll(Long userId, Boolean isActive, Pageable pageable);
}
