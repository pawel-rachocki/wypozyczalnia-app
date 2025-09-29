package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.Car;
import com.wypozyczalnia.car_rental_backend.model.entity.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    private Car availableCar;
    private Car rentedCar;

    @BeforeEach
    void setUp() {
        availableCar = new Car();
        availableCar.setBrand("Toyota");
        availableCar.setModel("Corolla");
        availableCar.setDailyPrice(BigDecimal.valueOf(100.00));
        availableCar.setStatus(CarStatus.DOSTEPNY);

        rentedCar = new Car();
        rentedCar.setBrand("Honda");
        rentedCar.setModel("Civic");
        rentedCar.setDailyPrice(BigDecimal.valueOf(120.00));
        rentedCar.setStatus(CarStatus.WYPOZYCZONY);

        entityManager.persistAndFlush(availableCar);
        entityManager.persistAndFlush(rentedCar);
    }

    @Test
    void shouldFindAvailableCarsSortedByBrandAndModel() {
        // given - dane w setUp()

        // when
        List<Car> result = carRepository.findByStatusOrderByBrandAscModelAsc(CarStatus.DOSTEPNY);

        // then
        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        assertEquals("Corolla", result.get(0).getModel());
        assertEquals(CarStatus.DOSTEPNY, result.get(0).getStatus());
    }

    @Test
    void shouldFindRentedCars() {
        // given - dane w setUp()

        // when
        List<Car> result = carRepository.findByStatusOrderByBrandAscModelAsc(CarStatus.WYPOZYCZONY);

        // then
        assertEquals(1, result.size());
        assertEquals("Honda", result.get(0).getBrand());
        assertEquals(CarStatus.WYPOZYCZONY, result.get(0).getStatus());
    }

    @Test
    void shouldReturnTrueWhenCarExists() {
        // given - dane w setUp()

        // when
        boolean exists = carRepository.existsByBrandIgnoreCaseAndModelIgnoreCase("TOYOTA", "corolla");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenCarDoesNotExist() {
        // given - dane w setUp()

        // when
        boolean exists = carRepository.existsByBrandIgnoreCaseAndModelIgnoreCase("BMW", "X5");

        // then
        assertFalse(exists);
    }

    @Test
    void shouldFindCarsByBrandIgnoreCase() {
        // given
        Car anotherToyota = new Car();
        anotherToyota.setBrand("Toyota");
        anotherToyota.setModel("Camry");
        anotherToyota.setDailyPrice(BigDecimal.valueOf(150.00));
        anotherToyota.setStatus(CarStatus.DOSTEPNY);
        entityManager.persistAndFlush(anotherToyota);

        // when
        List<Car> result = carRepository.findByBrandIgnoreCaseOrderByModelAsc("toyota");

        // then
        assertEquals(2, result.size());
        assertEquals("Camry", result.get(0).getModel());
        assertEquals("Corolla", result.get(1).getModel());
        result.forEach(samochod -> assertEquals("Toyota", samochod.getBrand()));
    }
}