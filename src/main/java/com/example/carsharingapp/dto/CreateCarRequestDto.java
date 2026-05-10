package com.example.carsharingapp.dto;

import com.example.carsharingapp.model.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Min(0)
    private int inventory;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal dailyFee;
}
