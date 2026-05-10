package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findAllByUserId(Long userId);

    Optional<Rental> findByUserIdAndActualReturnDateIsNull(Long userId);

    List<Rental> findAllByUserIdAndActualReturnDateIsNull(Long userId);

    List<Rental> findAllByUserIdAndActualReturnDateIsNotNull(Long userId);

    List<Rental> findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(LocalDate date);
}
