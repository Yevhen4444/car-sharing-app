package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarResponseDto create(CreateCarRequestDto requestDto);

    Page<CarResponseDto> getAll(Pageable pageable);

    CarResponseDto getById(Long id);

    CarResponseDto update(Long id, CreateCarRequestDto requestDto);

    void deleteById(Long id);
}
