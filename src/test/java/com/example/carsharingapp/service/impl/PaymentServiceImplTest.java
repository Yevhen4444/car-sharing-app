package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.StripePaymentService;
import com.example.carsharingapp.util.TestDataHelper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StripePaymentService stripePaymentService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void handleCancelledPaymentShouldReturnCancelMessage() {
        String actual = paymentService.handleCancelled();

        assertEquals("Payment was cancelled. You can pay later.", actual);
    }

    @Test
    void handleSuccessfulPaymentShouldThrowEntityNotFoundExceptionWhenPaymentNotFound() {
        String sessionId = "sessionId";

        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleSuccessful(sessionId));

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

        PaymentResponseDto actual = paymentService.handleSuccessful(sessionId);

        assertEquals(responseDto, actual);
        assertEquals(PaymentStatus.PAID, payment.getStatus());

        verify(paymentRepository).findBySessionId(sessionId);
        verify(notificationService).sendMessage(any());
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(savedPayment);
    }

    @Test
    void getPaymentsShouldReturnPaymentDtos() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Payment payment = TestDataHelper.createPayment();
        PaymentResponseDto responseDto = TestDataHelper.createPaymentResponseDto();

        Page<Payment> payments = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findAllByRentalUserId(userId, pageable))
                .thenReturn(payments);
        when(paymentMapper.toDto(payment))
                .thenReturn(responseDto);

        Page<PaymentResponseDto> actual = paymentService.getAll(userId, pageable);

        assertEquals(1, actual.getContent().size());
        assertEquals(responseDto, actual.getContent().get(0));

        verify(paymentRepository).findAllByRentalUserId(userId, pageable);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void getPaymentsShouldReturnEmptyPageWhenUserHasNoPayments() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> payments = Page.empty(pageable);

        when(paymentRepository.findAllByRentalUserId(userId, pageable))
                .thenReturn(payments);

        Page<PaymentResponseDto> actual = paymentService.getAll(userId, pageable);

        assertTrue(actual.isEmpty());

        verify(paymentRepository).findAllByRentalUserId(userId, pageable);
        verify(paymentMapper, times(0)).toDto(any());
    }
}
