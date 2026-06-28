package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findAllByUserId(Long userId, Pageable pageable);

    Optional<Rental> findByUserIdAndActualReturnDateIsNull(Long userId);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNull(Long userId, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNotNull(Long userId, Pageable pageable);

    List<Rental> findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(LocalDate date);
}
