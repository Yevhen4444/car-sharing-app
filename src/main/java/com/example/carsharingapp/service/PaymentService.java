package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    Page<PaymentResponseDto> getAll(Long userId, Pageable pageable);

    PaymentResponseDto create(CreatePaymentRequestDto dto);

    PaymentResponseDto handleSuccessful(String sessionId);

    String handleCancelled();


}
