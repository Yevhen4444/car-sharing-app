package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "CUSTOMER")
@Transactional
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "login@mail.com", roles = "CUSTOMER")
    void getById_ValidId_ShouldReturnRental() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("login@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper
                .createRentalWithoutId(savedUser, savedCar);

        Rental savedRental = rentalRepository.save(rental);

        mockMvc.perform(get("/rentals/" + savedRental.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRental.getId()))
                .andExpect(jsonPath("$.carId").value(savedCar.getId()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "login@mail.com", roles = "CUSTOMER")
    void create_ValidRequest_ShouldReturnCreated() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setPassword(passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        CreateRentalRequestDto requestDto =
                TestDataHelper.createRentalRequestDto();

        requestDto.setCarId(savedCar.getId());

        mockMvc.perform(post("/rentals")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
