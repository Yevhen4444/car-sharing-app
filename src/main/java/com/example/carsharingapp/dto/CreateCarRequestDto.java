package com.example.carsharingapp.dto;

import com.example.carsharingapp.model.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateCarRequestDto {

    @NotBlank
    private String model;

    @NotBlank
    private String brand;

    @NotNull
    private CarType carType;

    @Positive
    private int inventory;

    @NotNull
    @Positive
    private BigDecimal dailyFee;
}
