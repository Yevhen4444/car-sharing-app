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
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.RentalService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public RentalResponseDto create(CreateRentalRequestDto dto) {
        User user = getAuthenticatedUser();

        if (rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()).isPresent()) {
            throw new BadRequestException("User already has active rental " + user.getId());
        }

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found " + dto.getCarId()));

        if (car.getInventory() <= 0) {
            throw new BadRequestException("Car is not available " + dto.getCarId());
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
                        + ", car id: " + savedRental.getCar().getId());

        return rentalMapper.toDto(savedRental);
    }

    @Transactional
    @Override
    public RentalResponseDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found " + rentalId));

        if (rental.getActualReturnDate() != null) {
            throw new BadRequestException("Rental already returned " + rentalId);
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
                .orElseThrow(() -> new EntityNotFoundException("Rental not found " + rentalId));

        User currentUser = getAuthenticatedUser();

        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("You don't have access to this rental " + rentalId);
        }

        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalResponseDto> getAll(Long userId, Boolean isActive, Pageable pageable) {
        Page<Rental> rentals;

        if (isActive == null) {
            rentals = rentalRepository.findAllByUserId(userId, pageable);
        } else if (isActive) {
            rentals = rentalRepository.findAllByUserIdAndActualReturnDateIsNull(userId, pageable);
        } else {
            rentals = rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(userId, pageable);
        }

        return rentals.map(rentalMapper::toDto);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found by email: " + email));
    }
}
