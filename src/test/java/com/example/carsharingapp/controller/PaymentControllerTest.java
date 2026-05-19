package com.example.carsharingapp.controller;

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
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.StripePaymentService;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "CUSTOMER")
@Transactional
class PaymentControllerTest {

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

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void create_ValidRequest_ShouldReturnCreated() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("password123"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        rental.setActualReturnDate(LocalDate.now().plusDays(3));
        Rental savedRental = rentalRepository.save(rental);

        Session session = new Session();
        session.setId("session-id");
        session.setUrl("http://stripe-session");

        when(stripePaymentService.createSession(any())).thenReturn(session);

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
}
