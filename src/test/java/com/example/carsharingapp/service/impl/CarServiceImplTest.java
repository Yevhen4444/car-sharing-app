package com.example.carsharingapp.service.impl;

import com.example.carsharingapp.exception.EntityNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;

   @Test
   void createShouldSaveAndReturnCar() {
       Car car = TestDataHelper.createCar();
       when(carRepository.save(car)).thenReturn(car);
       Car result = carService.create(car);
       assertEquals(car, result);
       verify(carRepository, times(1)).save(car);
   }

   @Test
    void getAllShouldReturnAllCars() {
       Car firstCar = TestDataHelper.createCar();
       Car secondCar = TestDataHelper.createCar();
       List<Car> cars = List.of(firstCar, secondCar);
       when(carRepository.findAll()).thenReturn(cars);
       List<Car> result = carService.getAll();
       assertEquals(2, result.size());
       verify(carRepository, times(1)).findAll();
   }

   @Test
    void getByIdShouldReturnCarWhenCarExists() {
       Car car = TestDataHelper.createCar();
       when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
       Car result = carService.getById(car.getId());
       assertEquals(car, result);
       verify(carRepository, times(1)).findById(car.getId());
   }

   @Test
    void getByIdShouldThrowEntityNotFoundExceptionWhenCarDoesNotExist() {
       Long id = 1L;
       when(carRepository.findById(id)).thenReturn(Optional.empty());
       assertThrows(EntityNotFoundException.class, () -> carService.getById(id));
       verify(carRepository, times(1)).findById(id);
   }

   @Test
    void updateShouldSaveAndReturnCar() {
       Car car = TestDataHelper.createCar();
       when(carRepository.save(car)).thenReturn(car);
       Car result = carService.update(car);
       assertEquals(car, result);
       verify(carRepository, times(1)).save(car);
   }

   @Test
    void deleteByIdShouldCallRepositoryDeleteById() {
       Car car = TestDataHelper.createCar();
       carService.deleteById(car.getId());
       verify(carRepository, times(1)).deleteById(car.getId());
   }
}
