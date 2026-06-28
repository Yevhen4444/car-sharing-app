package com.example.carsharingapp.dto;

import com.example.carsharingapp.model.CarType;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarResponseDto {
    private Long id;
    private String model;
    private String brand;
    private CarType carType;
    private int inventory;
    private BigDecimal dailyFee;
}
