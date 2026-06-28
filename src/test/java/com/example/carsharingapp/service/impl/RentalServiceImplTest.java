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
import com.example.carsharingapp.util.TestDataHelper;
import java.time.LocalDate;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Test
    void createRentalShouldThrowBadRequestExceptionWhenUserHasActiveRental() {
        User user = TestDataHelper.createUser();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        CreateRentalRequestDto dto = TestDataHelper.createRentalRequestDto();

        Rental activeRental = TestDataHelper.createRental(
                user,
                TestDataHelper.createCar());

        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()))
                .thenReturn(Optional.of(activeRental));

        assertThrows(
                BadRequestException.class,
                () -> rentalService.create(dto));

        verify(rentalRepository)
                .findByUserIdAndActualReturnDateIsNull(user.getId());
    }

    @Test
    void createRentalShouldThrowEntityNotFoundExceptionWhenCarNotFound() {
        User user = TestDataHelper.createUser();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        CreateRentalRequestDto dto = TestDataHelper.createRentalRequestDto();

        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()))
                .thenReturn(Optional.empty());

        when(carRepository.findById(dto.getCarId()))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.create(dto));

        verify(rentalRepository)
                .findByUserIdAndActualReturnDateIsNull(user.getId());

        verify(carRepository)
                .findById(dto.getCarId());
    }

    @Test
    void createRentalShouldThrowBadRequestExceptionWhenCarInventoryIsZero() {
        User user = TestDataHelper.createUser();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        CreateRentalRequestDto dto = TestDataHelper.createRentalRequestDto();

        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()))
                .thenReturn(Optional.empty());

        Car car = TestDataHelper.createCarWithInventory(0);

        when(carRepository.findById(dto.getCarId()))
                .thenReturn(Optional.of(car));

        assertThrows(
                BadRequestException.class,
                () -> rentalService.create(dto));

        verify(rentalRepository)
                .findByUserIdAndActualReturnDateIsNull(user.getId());

        verify(carRepository)
                .findById(dto.getCarId());

        verify(rentalRepository, times(0))
                .save(any());

        verify(carRepository, times(0))
                .save(any());

        verify(notificationService, times(0))
                .sendMessage(any());
    }

    @Test
    void createRentalShouldCreateRentalSuccessfully() {
        User user = TestDataHelper.createUser();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        CreateRentalRequestDto dto = TestDataHelper.createRentalRequestDto();

        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(user.getId()))
                .thenReturn(Optional.empty());

        Car car = TestDataHelper.createCarWithInventory(5);

        when(carRepository.findById(dto.getCarId()))
                .thenReturn(Optional.of(car));

        Rental savedRental = TestDataHelper.createRental(user, car);

        RentalResponseDto rentalResponseDto =
                TestDataHelper.createRentalResponseDto();

        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(savedRental);

        when(rentalMapper.toDto(savedRental))
                .thenReturn(rentalResponseDto);

        RentalResponseDto actual = rentalService.create(dto);

        assertEquals(rentalResponseDto, actual);
        assertEquals(4, car.getInventory());

        verify(carRepository).save(car);
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationService).sendMessage(any());
        verify(rentalMapper).toDto(savedRental);
    }

    @Test
    void returnRentalShouldThrowEntityNotFoundExceptionWhenRentalNotFound() {
        Long rentalId = 1L;

        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.returnRental(rentalId));

        verify(rentalRepository).findById(rentalId);

        verify(carRepository, times(0)).save(any());
        verify(rentalRepository, times(0)).save(any());
    }

    @Test
    void returnRentalShouldThrowBadRequestExceptionWhenRentalAlreadyReturned() {
        Long rentalId = 1L;

        Rental rental = TestDataHelper.createRental(
                TestDataHelper.createUser(),
                TestDataHelper.createCar());

        rental.setActualReturnDate(LocalDate.now());

        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));

        assertThrows(
                BadRequestException.class,
                () -> rentalService.returnRental(rentalId));

        verify(rentalRepository).findById(rentalId);

        verify(carRepository, times(0)).save(any());
        verify(rentalRepository, times(0)).save(any());
    }

    @Test
    void returnRentalShouldReturnRentalSuccessfully() {
        Long rentalId = 1L;

        User user = TestDataHelper.createUser();

        Car car = TestDataHelper.createCarWithInventory(4);

        Rental rental = TestDataHelper.createRental(user, car);

        rental.setActualReturnDate(null);

        Rental savedRental = TestDataHelper.createRental(user, car);

        RentalResponseDto responseDto =
                TestDataHelper.createRentalResponseDto();

        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.save(rental))
                .thenReturn(savedRental);

        when(rentalMapper.toDto(savedRental))
                .thenReturn(responseDto);

        RentalResponseDto actual =
                rentalService.returnRental(rentalId);

        assertEquals(responseDto, actual);
        assertEquals(5, car.getInventory());

        verify(rentalRepository).findById(rentalId);
        verify(carRepository).save(car);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toDto(savedRental);
    }

    @Test
    void getByIdShouldThrowEntityNotFoundExceptionWhenRentalNotFound() {
        Long rentalId = 1L;

        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getById(rentalId));

        verify(rentalRepository).findById(rentalId);
    }

    @Test
    void getRentalsShouldReturnActiveRentals() {
        Long userId = 1L;
        Boolean isActive = true;

        Pageable pageable = PageRequest.of(0, 10);

        Rental rental = TestDataHelper.createRental(
                TestDataHelper.createUser(),
                TestDataHelper.createCar());

        RentalResponseDto responseDto =
                TestDataHelper.createRentalResponseDto();

        Page<Rental> rentals =
                new PageImpl<>(List.of(rental), pageable, 1);

        when(rentalRepository
                .findAllByUserIdAndActualReturnDateIsNull(userId, pageable))
                .thenReturn(rentals);

        when(rentalMapper.toDto(rental))
                .thenReturn(responseDto);

        Page<RentalResponseDto> actual =
                rentalService.getAll(userId, isActive, pageable);

        assertEquals(1, actual.getContent().size());

        assertEquals(
                responseDto,
                actual.getContent().get(0));

        verify(rentalRepository)
                .findAllByUserIdAndActualReturnDateIsNull(userId, pageable);

        verify(rentalMapper)
                .toDto(rental);
    }
}
