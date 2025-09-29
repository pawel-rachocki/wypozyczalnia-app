package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.Car;
import com.wypozyczalnia.car_rental_backend.model.entity.CarStatus;
import com.wypozyczalnia.car_rental_backend.model.exception.CarNotFoundException;
import com.wypozyczalnia.car_rental_backend.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Car testCar;
    private Car testCar2;

    @BeforeEach
    void setUp() {
        testCar = new Car();
        testCar.setId(1L);
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setDailyPrice(BigDecimal.valueOf(100.00));
        testCar.setStatus(CarStatus.DOSTEPNY);

        testCar2 = new Car();
        testCar2.setId(2L);
        testCar2.setBrand("Honda");
        testCar2.setModel("Civic");
        testCar2.setDailyPrice(BigDecimal.valueOf(120.00));
        testCar2.setStatus(CarStatus.WYPOZYCZONY);
    }

    @Test
    void shouldReturnAllCars() {
        // given
        List<Car> expectedSamochody = Arrays.asList(testCar, testCar2);
        when(carRepository.findAll()).thenReturn(expectedSamochody);

        // when
        List<Car> result = carService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        assertEquals("Honda", result.get(1).getBrand());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCars() {
        // given
        when(carRepository.findAll()).thenReturn(Arrays.asList());

        // when
        List<Car> result = carService.findAll();

        // then
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnCarWhenValidId() {
        // given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        // when
        Car result = carService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        assertEquals("Corolla", result.getModel());
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenInvalidId() {
        // given
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CarNotFoundException.class, () -> carService.findById(999L));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithNullBrand() {
        // given
        Car invalidCar = new Car(null, "Model", BigDecimal.valueOf(100.00), CarStatus.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.save(invalidCar)
        );

        assertEquals("Car Brand is required", exception.getMessage());
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithEmptyModel() {
        // given
        Car invalidCar = new Car("Toyota", "", BigDecimal.valueOf(100.00), CarStatus.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.save(invalidCar)
        );

        assertEquals("Car model is required", exception.getMessage());
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInvalidPrice() {
        // given
        Car invalidCar = new Car("Toyota", "Corolla", BigDecimal.valueOf(-50.00), CarStatus.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.save(invalidCar)
        );

        assertEquals("Daily Price must be greater than zero", exception.getMessage());
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCar() {
        // given
        Car updateData = new Car("Toyota", "Camry", BigDecimal.valueOf(150.00), CarStatus.DOSTEPNY);
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        CarNotFoundException exception = assertThrows(
                CarNotFoundException.class,
                () -> carService.update(999L, updateData)
        );

        assertEquals("Car with id 999 not found.", exception.getMessage());
        verify(carRepository, times(1)).findById(999L);
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void shouldDeleteAvailableCar() {
        // given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        doNothing().when(carRepository).deleteById(1L);

        // when
        carService.delete(1L);

        // then
        verify(carRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingRentedCar() {
        // given
        when(carRepository.findById(2L)).thenReturn(Optional.of(testCar2));

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> carService.delete(2L)
        );

        assertEquals("Cannot delete rented car", exception.getMessage());
        verify(carRepository, times(1)).findById(2L);
        verify(carRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCar() {
        // given
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        CarNotFoundException exception = assertThrows(
                CarNotFoundException.class,
                () -> carService.delete(999L)
        );

        assertEquals("Car with id 999 not found.", exception.getMessage());
        verify(carRepository, times(1)).findById(999L);
        verify(carRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnAvailableCars() {
        // given
        List<Car> availableCars = Arrays.asList(testCar);
        when(carRepository.findByStatusOrderByBrandAscModelAsc(CarStatus.DOSTEPNY)).thenReturn(availableCars);

        // when
        List<Car> result = carService.findAvailable();

        // then
        assertEquals(1, result.size());
        assertEquals(CarStatus.DOSTEPNY, result.get(0).getStatus());
        verify(carRepository, times(1)).findByStatusOrderByBrandAscModelAsc(CarStatus.DOSTEPNY);
    }

    @Test
    void shouldMarkCarAsRented() {
        // given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // when
        carService.markAsRented(1L);

        // then
        assertEquals(CarStatus.WYPOZYCZONY, testCar.getStatus());
        verify(carRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).save(testCar);
    }

    @Test
    void shouldThrowExceptionWhenMarkingAlreadyRentedCar() {
        // given
        when(carRepository.findById(2L)).thenReturn(Optional.of(testCar2));

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> carService.markAsRented(2L)
        );

        assertEquals("Car is not available for rental", exception.getMessage());
        verify(carRepository, times(1)).findById(2L);
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void shouldMarkCarAsAvailable() {
        // given
        when(carRepository.findById(2L)).thenReturn(Optional.of(testCar2));
        when(carRepository.save(any(Car.class))).thenReturn(testCar2);

        // when
        carService.markAsAvailable(2L);

        // then
        assertEquals(CarStatus.DOSTEPNY, testCar2.getStatus());
        verify(carRepository, times(1)).findById(2L);
        verify(carRepository, times(1)).save(testCar2);
    }

    @Test
    void shouldReturnTrueWhenCarIsAvailable() {
        // given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        // when
        boolean result = carService.isAvailable(1L);

        // then
        assertTrue(result);
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnFalseWhenCarIsRented() {
        // given
        when(carRepository.findById(2L)).thenReturn(Optional.of(testCar2));

        // when
        boolean result = carService.isAvailable(2L);

        // then
        assertFalse(result);
        verify(carRepository, times(1)).findById(2L);
    }

    @Test
    void shouldReturnFalseWhenCarNotExists() {
        // given
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        boolean result = carService.isAvailable(999L);

        // then
        assertFalse(result);
        verify(carRepository, times(1)).findById(999L);
    }
}