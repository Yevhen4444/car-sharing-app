package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize ("hasAnyRole('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get all payments")
    @GetMapping
    public Page<PaymentResponseDto> getAll(@RequestParam(name = "user_id") Long userId, Pageable pageable) {
        return paymentService.getAll(userId, pageable);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new payment")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto create(@Valid @RequestBody CreatePaymentRequestDto dto) {
        return paymentService.create(dto);
    }

    @Operation(summary = "Handle successful payment")
    @GetMapping("/success")
    public PaymentResponseDto handleSuccess(@RequestParam(name = "session_id") String sessionId) {
        return paymentService.handleSuccessful(sessionId);
    }

    @Operation(summary = "Handle cancelled payment")
    @GetMapping("/cancel")
    public String handleCancel() {
        return paymentService.handleCancelled();
    }
}
