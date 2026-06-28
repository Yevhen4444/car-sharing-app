package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.dto.CarResponseDto;
import com.example.carsharingapp.dto.CreateCarRequestDto;
import com.example.carsharingapp.exception.EntityNotFoundException;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.util.TestDataHelper;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Test
    void createShouldSaveAndReturnCar() {
        CreateCarRequestDto requestDto = TestDataHelper.createCarRequestDto();
        Car car = TestDataHelper.createCar();
        CarResponseDto responseDto = TestDataHelper.createCarResponseDto();

        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.create(requestDto);

        assertEquals(responseDto, result);
        verify(carMapper).toEntity(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    void getAllShouldReturnAllCars() {
        Car firstCar = TestDataHelper.createCar();
        Car secondCar = TestDataHelper.createCar();
        CarResponseDto firstDto = TestDataHelper.createCarResponseDto();
        CarResponseDto secondDto = TestDataHelper.createCarResponseDto();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> cars = new PageImpl<>(List.of(firstCar, secondCar), pageable, 2);

        when(carRepository.findAll(pageable)).thenReturn(cars);
        when(carMapper.toDto(firstCar)).thenReturn(firstDto);
        when(carMapper.toDto(secondCar)).thenReturn(secondDto);

        Page<CarResponseDto> result = carService.getAll(pageable);

        assertEquals(2, result.getContent().size());
        verify(carRepository).findAll(pageable);
    }

    @Test
    void getByIdShouldReturnCarWhenCarExists() {
        Car car = TestDataHelper.createCar();
        CarResponseDto responseDto = TestDataHelper.createCarResponseDto();

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.getById(car.getId());

        assertEquals(responseDto, result);
        verify(carRepository).findById(car.getId());
        verify(carMapper).toDto(car);
    }

    @Test
    void getByIdShouldThrowEntityNotFoundExceptionWhenCarDoesNotExist() {
        Long id = 1L;

        when(carRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carService.getById(id));
        verify(carRepository).findById(id);
    }

    @Test
    void updateShouldUpdateAndReturnCar() {
        Long id = 1L;
        CreateCarRequestDto requestDto = TestDataHelper.createCarRequestDto();
        Car car = TestDataHelper.createCar();
        CarResponseDto responseDto = TestDataHelper.createCarResponseDto();

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.update(id, requestDto);

        assertEquals(responseDto, result);
        verify(carRepository).findById(id);
        verify(carMapper).updateCarFromDto(requestDto, car);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    void deleteByIdShouldCallRepositoryDeleteById() {
        Car car = TestDataHelper.createCar();

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));

        carService.deleteById(car.getId());

        verify(carRepository).delete(car);
    }
}