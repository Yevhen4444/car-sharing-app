package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.util.TestDataHelper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    void handleCancelledPaymentShouldReturnCancelMessage() {
        String actual = paymentService.handleCancelledPayment();
        assertEquals("Payment was cancelled. You can pay later.", actual);
    }

    @Test
    void handleSuccessfulPaymentShouldThrowEntityNotFoundExceptionWhenPaymentNotFound() {
        String sessionId = "sessionId";
        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleSuccessfulPayment(sessionId));
        verify(paymentRepository).findBySessionId(sessionId);
    }

    @Test
    void handleSuccessfulPaymentShouldMarkPaymentAsPaid() {
        String sessionId = "session-id";
        Payment payment = TestDataHelper.createPayment();
        Payment savedPayment = TestDataHelper.createPayment();
        savedPayment.setStatus(PaymentStatus.PAID);
        PaymentResponseDto responseDto = TestDataHelper.createPaymentResponseDto();
        responseDto.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment))
                .thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment))
                .thenReturn(responseDto);
        PaymentResponseDto actual = paymentService.handleSuccessfulPayment(sessionId);
        assertEquals(responseDto, actual);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository, times(1))
                .findBySessionId(sessionId);
        verify(notificationService, times(1))
                .sendMessage(any());
        verify(paymentRepository, times(1))
                .save(payment);
        verify(paymentMapper, times(1))
                .toDto(savedPayment);
    }

    @Test
    void getPaymentsShouldReturnPaymentDtos() {
        Long userId = 1L;
        Payment payment = TestDataHelper.createPayment();
        PaymentResponseDto responseDto = TestDataHelper.createPaymentResponseDto();
        List<Payment> payments = List.of(payment);
        List<PaymentResponseDto> responseDtos = List.of(responseDto);
        when(paymentRepository.findAllByRentalUserId(userId))
                .thenReturn(payments);
        when(paymentMapper.toDto(payment))
                .thenReturn(responseDto);
        List<PaymentResponseDto> actual = paymentService.getPayments(userId);
        assertEquals(responseDtos, actual);
        verify(paymentRepository, times(1))
                .findAllByRentalUserId(userId);
        verify(paymentMapper, times(1))
                .toDto(payment);
    }

    @Test
    void getPaymentsShouldReturnEmptyListWhenUserHasNoPayments() {
        Long userId = 1L;
        when(paymentRepository.findAllByRentalUserId(userId))
                .thenReturn(List.of());
        List<PaymentResponseDto> actual =
                paymentService.getPayments(userId);
        assertTrue(actual.isEmpty());
        verify(paymentRepository, times(1))
                .findAllByRentalUserId(userId);
        verify(paymentMapper, times(0))
                .toDto(any());
    }
}
