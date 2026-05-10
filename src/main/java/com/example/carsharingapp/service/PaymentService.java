package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import java.util.List;

public interface PaymentService {

    List<PaymentResponseDto> getPayments(Long userId);

    PaymentResponseDto createPayment(CreatePaymentRequestDto dto);

    PaymentResponseDto handleSuccessfulPayment(String sessionId);

    String handleCancelledPayment();


}
