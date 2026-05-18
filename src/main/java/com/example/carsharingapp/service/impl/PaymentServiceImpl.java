package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.exception.BadRequestException;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.PaymentService;
import com.example.carsharingapp.service.StripePaymentService;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2);

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;
    private final StripePaymentService stripePaymentService;

    @Override
    public Page<PaymentResponseDto> getAll(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAllByRentalUserId(userId, pageable);
        return payments.map(paymentMapper::toDto);
    }

    @Override
    public PaymentResponseDto create(CreatePaymentRequestDto dto) {
        Rental rental = getRentalById(dto.getRentalId());
        validateRentalReturned(rental);

        BigDecimal amount = calculateAmount(dto.getType(), rental);
        Session session = stripePaymentService.createSession(amount);

        Payment payment = createPayment(dto, rental, amount, session);
        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentResponseDto handleSuccessful(String sessionId) {
        Payment payment = getPaymentBySessionId(sessionId);
        payment.setStatus(PaymentStatus.PAID);

        notificationService.sendMessage(
                "Payment successful. Payment id: " + payment.getId()
                        + ", rental id: " + payment.getRental().getId());

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public String handleCancelled() {
        return "Payment was cancelled. You can pay later.";
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found by id: " + rentalId));
    }

    private Payment getPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment with session id: " + sessionId + " was not found"));
    }

    private void validateRentalReturned(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            throw new BadRequestException("Rental is not returned yet");
        }
    }

    private BigDecimal calculateAmount(PaymentType type, Rental rental) {
        if (PaymentType.PAYMENT.equals(type)) {
            return calculatePaymentAmount(rental);
        }
        return calculateFineAmount(rental);
    }

    private BigDecimal calculatePaymentAmount(Rental rental) {
        long rentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getActualReturnDate());

        if (rentalDays == 0) {
            rentalDays = 1;
        }

        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(rentalDays));
    }

    private BigDecimal calculateFineAmount(Rental rental) {
        long overdueDays = ChronoUnit.DAYS.between(
                rental.getReturnDate(),
                rental.getActualReturnDate());

        if (overdueDays <= 0) {
            throw new BadRequestException("Rental is not overdue");
        }

        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(overdueDays))
                .multiply(FINE_MULTIPLIER);
    }

    private Payment createPayment(
            CreatePaymentRequestDto dto,
            Rental rental,
            BigDecimal amount,
            Session session) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRental(rental);
        payment.setAmountToPay(amount);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        return payment;
    }
}
