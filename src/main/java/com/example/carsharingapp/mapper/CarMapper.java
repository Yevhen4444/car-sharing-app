package com.example.carsharingapp.mapper;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    Car toEntity(CreateCarRequestDto dto);

    CarResponseDto toDto(Car car);

    void updateCarFromDto(CreateCarRequestDto requestDto, @MappingTarget Car car);
}
