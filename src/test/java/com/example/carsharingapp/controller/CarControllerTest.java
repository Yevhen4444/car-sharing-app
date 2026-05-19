package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.util.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MANAGER")
@Transactional
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Test
    void createCar_ValidRequest_ShouldReturnCreated() throws Exception {
        CreateCarRequestDto requestDto = TestDataHelper.createCarRequestDto();

        mockMvc.perform(post("/cars")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.model").value(requestDto.getModel()))
                .andExpect(jsonPath("$.brand").value(requestDto.getBrand()))
                .andExpect(jsonPath("$.carType")
                        .value(requestDto.getCarType().toString()))
                .andExpect(jsonPath("$.inventory")
                        .value(requestDto.getInventory()))
                .andExpect(jsonPath("$.dailyFee").value(100));
    }

    @Test
    void getAll_ShouldReturnPageOfCars() throws Exception {
        Car car = TestDataHelper.createCarWithoutId();
        carRepository.save(car);

        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].model")
                        .value(car.getModel()))
                .andExpect(jsonPath("$.content[0].brand")
                        .value(car.getBrand()));
    }

    @Test
    void getById_ValidId_ShouldReturnCar() throws Exception {
        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        mockMvc.perform(get("/cars/" + savedCar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedCar.getId()))
                .andExpect(jsonPath("$.model")
                        .value(savedCar.getModel()))
                .andExpect(jsonPath("$.brand")
                        .value(savedCar.getBrand()));
    }

    @Test
    void update_ValidRequest_ShouldReturnUpdatedCar() throws Exception {
        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        CreateCarRequestDto updatedDto = TestDataHelper.createCarRequestDto();
        updatedDto.setModel("BMW");
        updatedDto.setBrand("X5");

        mockMvc.perform(put("/cars/" + savedCar.getId())
                        .content(objectMapper.writeValueAsString(updatedDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("BMW"))
                .andExpect(jsonPath("$.brand").value("X5"));
    }

    @Test
    void delete_ValidId_ShouldReturnNoContent() throws Exception {
        Car car = TestDataHelper.createCarWithoutId();
        Car savedCar = carRepository.save(car);

        mockMvc.perform(delete("/cars/" + savedCar.getId()))
                .andExpect(status().isNoContent());

        boolean exists = carRepository.existsById(savedCar.getId());

        assertFalse(exists);
    }
}
