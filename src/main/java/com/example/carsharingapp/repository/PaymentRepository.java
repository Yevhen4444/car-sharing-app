package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByRentalUserId(Long userId);

    Optional<Payment> findBySessionId(String sessionId);
}
