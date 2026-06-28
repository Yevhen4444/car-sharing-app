package com.example.carsharingapp.dto;

import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private String sessionUrl;
    private String sessionId;
    private PaymentStatus status;
    private PaymentType type;
    private Long rentalId;
    private BigDecimal amountToPay;
}
