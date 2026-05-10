package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.service.CarService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;
    private final CarMapper carMapper;

    @PostMapping
    public CarResponseDto create(@Valid @RequestBody CreateCarRequestDto requestDto) {
        Car car = carMapper.toEntity(requestDto);
        Car savedCar = carService.create(car);
        return carMapper.toDto(savedCar);
    }

 @GetMapping
    public List<CarResponseDto> getAll() {

    return carService.getAll()
            .stream()
            .map(carMapper::toDto)
            .toList();
 }

@GetMapping("/{id}")
public CarResponseDto getById(@PathVariable long id) {
    Car car = carService.getById(id);
    return carMapper.toDto(car);
}

    @PutMapping("/{id}")
    public CarResponseDto update(@PathVariable Long id,
                                 @Valid @RequestBody CreateCarRequestDto requestDto) {
        Car car = carMapper.toEntity(requestDto);
        car.setId(id);
        Car updatedCar = carService.update(car);
        return carMapper.toDto(updatedCar);
    }

@DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
    carService.deleteById(id);
  }
}
