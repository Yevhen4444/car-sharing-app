package com.example.carsharingapp.util;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.dto.PaymentResponseDto;
import com.example.carsharingapp.dto.RentalResponseDto;
import com.example.carsharingapp.dto.UserLoginRequestDto;
import com.example.carsharingapp.dto.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.UserRegistrationResponseDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.CarType;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataHelper {

    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    public static User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("test" + id + "@mail.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    public static Car createCar() {
        Car car = new Car();
        car.setId(1L);
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(100));
        return car;
    }

    public static Car createCarWithInventory(int inventory) {
        Car car = createCar();
        car.setInventory(inventory);
        return car;
    }

    public static Rental createRental(User user, Car car) {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setActualReturnDate(null);
        rental.setUser(user);
        rental.setCar(car);
        return rental;
    }

    public static Rental createReturnedRental(User user, Car car) {
        Rental rental = createRental(user, car);
        rental.setActualReturnDate(LocalDate.now());
        return rental;
    }

    public static CreateRentalRequestDto createRentalRequestDto() {
        CreateRentalRequestDto dto = new CreateRentalRequestDto();
        dto.setCarId(1L);
        dto.setReturnDate(LocalDate.now().plusDays(5));
        return dto;
    }

    public static RentalResponseDto createRentalResponseDto() {
        RentalResponseDto dto = new RentalResponseDto();
        dto.setId(1L);
        dto.setRentalDate(LocalDate.now().toString());
        dto.setReturnDate(LocalDate.now().plusDays(5).toString());
        dto.setActualReturnDate(null);
        dto.setCarId(1L);
        dto.setUserId(1L);
        return dto;
    }

    public static User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    public static UserLoginRequestDto createUserLoginRequestDto(String email, String password) {
        UserLoginRequestDto dto = new UserLoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    public static UserRegistrationRequestDto createUserRegistrationRequestDto(
            String email,
            String password,
            String repeatPassword) {
        UserRegistrationRequestDto dto = new UserRegistrationRequestDto();
        dto.setEmail(email);
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPassword(password);
        dto.setRepeatPassword(repeatPassword);
        return dto;
    }

    public static UserRegistrationResponseDto createUserRegistrationResponseDto(Long id, String email, String firstName, String lastName) {
        UserRegistrationResponseDto dto = new UserRegistrationResponseDto();
        dto.setId(id);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        return dto;
    }

    public static Payment createPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(PaymentType.PAYMENT);
        payment.setAmountToPay(BigDecimal.valueOf(100));
        payment.setSessionId("session-id");
        payment.setSessionUrl("http://stripe-session");
        Rental rental = createRental(createUser(), createCar());
        payment.setRental(rental);
        return payment;
    }

    public static PaymentResponseDto createPaymentResponseDto() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(1L);
        dto.setStatus(PaymentStatus.PENDING);
        dto.setType(PaymentType.PAYMENT);
        dto.setRentalId(1L);
        dto.setSessionUrl("http://stripe-session");
        dto.setAmountToPay(BigDecimal.valueOf(100));
        return dto;
    }

    public static CreateCarRequestDto createCarRequestDto() {
        CreateCarRequestDto dto = new CreateCarRequestDto();
        dto.setModel("Model S");
        dto.setBrand("Tesla");
        dto.setCarType(CarType.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(100));
        return dto;
    }

    public static CarResponseDto createCarResponseDto() {
        CarResponseDto dto = new CarResponseDto();
        dto.setId(1L);
        dto.setModel("Model S");
        dto.setBrand("Tesla");
        dto.setCarType(CarType.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(100));
        return dto;
    }

    public static User createUserWithoutId() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    public static Car createCarWithoutId() {
        Car car = new Car();
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(100));
        return car;
    }

    public static Rental createRentalWithoutId(User user, Car car) {
        Rental rental = new Rental();
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setActualReturnDate(null);
        rental.setUser(user);
        rental.setCar(car);
        return rental;
    }

    public static Payment createPaymentWithoutId(Rental rental) {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(PaymentType.PAYMENT);
        payment.setAmountToPay(BigDecimal.valueOf(100));
        payment.setSessionId("session-id");
        payment.setSessionUrl("http://stripe-session");
        payment.setRental(rental);
        return payment;
    }
}
