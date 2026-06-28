package com.example.carsharingapp.service.notification;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverdueRentalScheduler {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals =
                rentalRepository.findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(today);
        if (overdueRentals.isEmpty()) {
            notificationService.sendMessage("No rentals overdue today!");
            return;
        }
        for (Rental rental : overdueRentals) {
            notificationService.sendMessage(
                    "Overdue rental! Rental id: " + rental.getId()
                            + ", user id: " + rental.getUser().getId()
                            + ", car id: " + rental.getCar().getId()
                            + ", return date: " + rental.getReturnDate());
        }
    }
}