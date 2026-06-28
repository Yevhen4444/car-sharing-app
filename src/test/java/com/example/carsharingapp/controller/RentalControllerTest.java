package com.example.carsharingapp.controller;

import com.example.carsharingapp.config.MySqlTestContainer;
import com.example.carsharingapp.dto.CreateRentalRequestDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.impl.TelegramNotificationService;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "CUSTOMER")
class RentalControllerTest extends MySqlTestContainer {

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

    @MockBean
    private TelegramNotificationService telegramNotificationService;

    @Test
    @WithMockUser(username = "getbyid@mail.com", roles = "CUSTOMER")
    void getById_ValidId_ShouldReturnRental() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("getbyid@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        Rental rental = TestDataHelper.createRentalWithoutId(savedUser, savedCar);
        Rental savedRental = rentalRepository.save(rental);

        mockMvc.perform(get("/rentals/" + savedRental.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRental.getId()))
                .andExpect(jsonPath("$.carId").value(savedCar.getId()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "create@mail.com", roles = "CUSTOMER")
    void create_ValidRequest_ShouldReturnCreated() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("create@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        CreateRentalRequestDto requestDto = TestDataHelper.createRentalRequestDto();
        requestDto.setCarId(savedCar.getId());

        mockMvc.perform(post("/rentals")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getById_InvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/rentals/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "invalidcar@mail.com", roles = "CUSTOMER")
    void create_InvalidCar_ShouldReturnNotFound() throws Exception {
        User user = TestDataHelper.createUserWithoutId();
        user.setEmail("invalidcar@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        CreateRentalRequestDto dto = TestDataHelper.createRentalRequestDto();
        dto.setCarId(999999L);

        mockMvc.perform(post("/rentals")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
