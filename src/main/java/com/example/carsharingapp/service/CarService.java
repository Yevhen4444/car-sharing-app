package com.example.carsharingapp.service;

import com.example.carsharingapp.model.Car;
import java.util.List;

public interface CarService {
    Car create(Car car);

    List<Car> getAll();

    Car getById(Long id);

    Car update(Car car);

    void deleteById(Long id);
}
