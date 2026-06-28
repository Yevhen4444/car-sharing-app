package com.example.carsharingapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;

    @Future
    @NotNull
    private LocalDate returnDate;
}
