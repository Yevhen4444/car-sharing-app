package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import com.example.carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals", description = "Endpoints for managing rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new rental")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponseDto create(@Valid @RequestBody CreateRentalRequestDto dto) {
        return rentalService.create(dto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Return rental")
    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get rental by id")
    @GetMapping("/{id}")
    public RentalResponseDto getById(@PathVariable Long id) {
        return rentalService.getById(id);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get all rentals")
    @GetMapping
    public Page<RentalResponseDto> getAll(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            Pageable pageable) {
        return rentalService.getAll(userId, isActive, pageable);
    }
}
