package com.example.carsharingapp.controller;

import com.example.carsharingapp.config.MySqlTestContainer;
import com.example.carsharingapp.dto.CreatePaymentRequestDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentType;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.StripePaymentService;
import com.example.carsharingapp.service.impl.TelegramNotificationService;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "CUSTOMER")
class PaymentControllerTest extends MySqlTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private StripePaymentService stripePaymentService;

    @MockBean
    private TelegramNotificationService telegramNotificationService;

    @Test
    void create_ValidRequest_ShouldReturnCreated() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("http://stripe-session");
        when(mockSession.getId()).thenReturn("test-session-id");
        when(stripePaymentService.createSession(any())).thenReturn(mockSession);

        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("password123"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        rental.setActualReturnDate(LocalDate.now().plusDays(3));
        Rental savedRental = rentalRepository.save(rental);

        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(savedRental.getId());
        requestDto.setType(PaymentType.PAYMENT);

        mockMvc.perform(post("/payments")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rentalId").value(savedRental.getId()))
                .andExpect(jsonPath("$.sessionUrl").value("http://stripe-session"));
    }

    @Test
    void getAll_ValidUserId_ShouldReturnPageOfPayments() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("password123"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        Rental savedRental = rentalRepository.save(rental);

        Payment payment = TestDataHelper.createPaymentWithoutId(savedRental);
        paymentRepository.save(payment);

        mockMvc.perform(get("/payments")
                        .param("user_id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rentalId")
                        .value(savedRental.getId()));
    }

    @Test
    void handleSuccess_ValidSessionId_ShouldReturnPaidPayment() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("password123"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        Rental savedRental = rentalRepository.save(rental);

        Payment payment = TestDataHelper.createPaymentWithoutId(savedRental);
        payment.setSessionId("session-id");
        paymentRepository.save(payment);

        mockMvc.perform(get("/payments/success")
                        .param("session_id", "session-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void handleCancel_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/payments/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was cancelled. You can pay later."));
    }

    @Test
    void handleSuccess_InvalidSession_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/payments/success")
                        .param("session_id", "wrong-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_NotReturnedRental_ShouldReturnBadRequest() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("123"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        rental.setActualReturnDate(null);
        rentalRepository.save(rental);

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setRentalId(rental.getId());
        dto.setType(PaymentType.PAYMENT);

        mockMvc.perform(post("/payments")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
