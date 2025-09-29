package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.Car;
import com.wypozyczalnia.car_rental_backend.model.entity.CarStatus;
import com.wypozyczalnia.car_rental_backend.model.exception.CarNotFoundException;
import com.wypozyczalnia.car_rental_backend.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;

    public List<Car> findAll() {
        return carRepository.findAll();
    }

    public Car findById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));
    }

    @Transactional
    public Car save(Car car) {
        validateSamochod(car);

        if (car.getStatus() == null) {
            car.setStatus(CarStatus.DOSTEPNY);
        }

        return carRepository.save(car);
    }

    @Transactional
    public Car update(Long id, Car carUpdate) {
        Car existing = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        validateSamochod(carUpdate);

        existing.setBrand(carUpdate.getBrand());
        existing.setModel(carUpdate.getModel());
        existing.setDailyPrice(carUpdate.getDailyPrice());
        existing.setStatus(carUpdate.getStatus());

        return carRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        if (CarStatus.WYPOZYCZONY.equals(car.getStatus())) {
            throw new IllegalStateException("Cannot delete rented car");
        }

        carRepository.deleteById(id);
    }

    public List<Car> findAvailable() {
        return carRepository.findByStatusOrderByBrandAscModelAsc(CarStatus.DOSTEPNY);
    }

    @Transactional
    public void markAsRented(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        if (!CarStatus.DOSTEPNY.equals(car.getStatus())) {
            throw new IllegalStateException("Car is not available for rental");
        }

        car.setStatus(CarStatus.WYPOZYCZONY);
        carRepository.save(car);
    }

    @Transactional
    public void markAsAvailable(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));

        car.setStatus(CarStatus.DOSTEPNY);
        carRepository.save(car);
    }

    public boolean isAvailable(Long id) {
        return carRepository.findById(id)
                .map(samochod -> CarStatus.DOSTEPNY.equals(samochod.getStatus()))
                .orElse(false);
    }

    private void validateSamochod(Car car) {
        if (car.getBrand() == null || car.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Car Brand is required");
        }

        if (car.getModel() == null || car.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Car model is required");
        }

        if (car.getDailyPrice() == null || car.getDailyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Daily Price must be greater than zero");
        }
    }
}
