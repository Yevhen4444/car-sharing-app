package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.service.CarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars", description = "Endpoints for managing cars")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponseDto create(@Valid @RequestBody CreateCarRequestDto requestDto) {
        return carService.create(requestDto);
    }

    @GetMapping
    public Page<CarResponseDto> getAll(Pageable pageable) {
        return carService.getAll(pageable);

    }

    @GetMapping("/{id}")
    public CarResponseDto getById(@PathVariable long id) {
        return carService.getById(id);

    }

    @PutMapping("/{id}")
    public CarResponseDto update(@PathVariable Long id,
                                 @Valid @RequestBody CreateCarRequestDto requestDto) {
        return carService.update(id, requestDto);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
