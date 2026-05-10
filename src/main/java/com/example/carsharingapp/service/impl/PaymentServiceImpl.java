package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.exception.BadRequestException;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.exception.PaymentException;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public List<PaymentResponseDto> getPayments(Long userId) {
        List<Payment> payments = paymentRepository.findAllByRentalUserId(userId);
        return payments.stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentResponseDto createPayment(CreatePaymentRequestDto dto) {
        Rental rental = rentalRepository.findById(dto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found by id: " + dto.getRentalId()));

        if (rental.getActualReturnDate() == null) {
            throw new BadRequestException("Rental is not returned yet");
        }

        BigDecimal amount;

        if (PaymentType.PAYMENT.equals(dto.getType())) {
            long rentalDays = ChronoUnit.DAYS.between(
                    rental.getRentalDate(),
                    rental.getActualReturnDate()
            );

            if (rentalDays == 0) {
                rentalDays = 1;
            }

            amount = rental.getCar().getDailyFee()
                    .multiply(BigDecimal.valueOf(rentalDays));
        } else {
            long overdueDays = ChronoUnit.DAYS.between(
                    rental.getReturnDate(),
                    rental.getActualReturnDate()
            );

            if (overdueDays <= 0) {
                throw new BadRequestException("Rental is not overdue");
            }

            BigDecimal fineMultiplier = BigDecimal.valueOf(2);

            amount = rental.getCar().getDailyFee()
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(fineMultiplier);
        }
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Car rental payment")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new PaymentException("Can't create Stripe session", e);
        }
        Payment payment = new Payment();
        payment.setType(dto.getType());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRental(rental);
        payment.setAmountToPay(amount);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentResponseDto handleSuccessfulPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.PAID);
        notificationService.sendMessage(
                "Payment successful. Payment id: " + payment.getId()
                        + ", rental id: " + payment.getRental().getId()
        );
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);    }

    @Override
    public String handleCancelledPayment() {
        return "Payment was cancelled. You can pay later.";
    }
}
