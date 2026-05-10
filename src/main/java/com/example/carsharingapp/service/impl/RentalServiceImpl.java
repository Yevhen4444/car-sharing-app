package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import com.example.carsharingapp.exception.BadRequestException;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.RentalService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public RentalResponseDto createRental(CreateRentalRequestDto dto) {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()).isPresent()) {
            throw new BadRequestException("User already has active rental");
        }
        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found"));
        if (car.getInventory() <= 0) {
            throw new BadRequestException("Car is not available");
        }
        car.setInventory(car.getInventory() - 1);
        Rental rental = new Rental();
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(dto.getReturnDate());
        rental.setActualReturnDate(null);
        rental.setUser(user);
        rental.setCar(car);
        carRepository.save(car);
        Rental savedRental = rentalRepository.save(rental);
        notificationService.sendMessage(
                "New rental created. Rental id: " + savedRental.getId()
                        + ", user id: " + savedRental.getUser().getId()
                        + ", car id: " + savedRental.getCar().getId()
        );
        return rentalMapper.toDto(savedRental);
    }

    @Transactional
    @Override
    public RentalResponseDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));
        if (rental.getActualReturnDate() != null) {
            throw new BadRequestException("Rental already returned");
        }
        rental.setActualReturnDate(LocalDate.now());
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        Rental savedRental = rentalRepository.save(rental);
        return rentalMapper.toDto(savedRental);
    }

    @Override
    public RentalResponseDto getById(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));
       User currentUser = (User) SecurityContextHolder.getContext()
               .getAuthentication()
               .getPrincipal();
        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("You don't have access to this rental");
        }
        return rentalMapper.toDto(rental);
    }

    @Override
    public List<RentalResponseDto> getRentals(Long userId, Boolean isActive) {
        List<Rental> rentals;

        if (isActive == null) {
            rentals = rentalRepository.findAllByUserId(userId);
        } else if (isActive) {
            rentals = rentalRepository.findAllByUserIdAndActualReturnDateIsNull(userId);
        } else {
            rentals = rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(userId);
        }

        return rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
    }
}
