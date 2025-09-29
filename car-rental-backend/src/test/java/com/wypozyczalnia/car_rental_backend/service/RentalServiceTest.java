package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.*;
import com.wypozyczalnia.car_rental_backend.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarService carService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private RentalService rentalService;

    private Rental testRental;
    private Client testClient;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFirstName("Jan");
        testClient.setLastName("Kowalski");
        testClient.setEmail("jan.kowalski@email.com");

        testCar = new Car();
        testCar.setId(1L);
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setDailyPrice(BigDecimal.valueOf(100.00));
        testCar.setStatus(CarStatus.DOSTEPNY);

        testRental = new Rental();
        testRental.setId(1L);
        testRental.setClient(testClient);
        testRental.setCar(testCar);
        testRental.setRentalDate(LocalDate.now());
        testRental.setReturnDate(LocalDate.now().plusDays(3));
        testRental.setTotalCost(BigDecimal.valueOf(300.00));
        testRental.setStatus(RentalStatus.AKTYWNE);
    }

    @Test
    void shouldReturnAllRentals() {
        // given
        List<Rental> expectedRentals = Arrays.asList(testRental);
        when(rentalRepository.findAll()).thenReturn(expectedRentals);

        // when
        List<Rental> result = rentalService.findAll();

        // then
        assertEquals(1, result.size());
        assertEquals(testClient, result.get(0).getClient());
        verify(rentalRepository, times(1)).findAll();
    }

    @Test
    void shouldSuccessfullyRentCar() {
        // given
        LocalDate rentalDate = LocalDate.now();
        LocalDate plannedReturnDate = LocalDate.now().plusDays(3);

        when(clientService.findById(1L)).thenReturn(testClient);
        when(carService.findById(1L)).thenReturn(testCar);
        when(rentalRepository.existsByCarIdAndStatus(1L, RentalStatus.AKTYWNE)).thenReturn(false);
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        doNothing().when(carService).markAsRented(1L);

        // when
        Rental result = rentalService.rentCar(1L, 1L, rentalDate, plannedReturnDate);

        // then
        assertNotNull(result);
        assertEquals(testClient, result.getClient());
        assertEquals(testCar, result.getCar());
        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(carService, times(1)).markAsRented(1L);
    }

    @Test
    void shouldThrowExceptionWhenRentingWithInvalidDates() {
        // given
        LocalDate rentalDate = LocalDate.now();
        LocalDate plannedReturnDate = LocalDate.now().minusDays(1); // Data w przeszłości

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.rentCar(1L, 1L, rentalDate, plannedReturnDate)
        );

        assertEquals("Return date must be later than rental date", exception.getMessage());
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void shouldSuccessfullyReturnCar() {
        // given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        doNothing().when(carService).markAsAvailable(1L);

        // when
        Rental result = rentalService.returnCar(1L);

        // then
        assertNotNull(result);
        assertEquals(RentalStatus.ZAKONCZONE, testRental.getStatus());
        verify(rentalRepository, times(1)).save(testRental);
        verify(carService, times(1)).markAsAvailable(1L);
    }

    @Test
    void shouldReturnActiveRentals() {
        // given
        List<Rental> activeRentals = Arrays.asList(testRental);
        when(rentalRepository.findByStatusOrderByRentalDateDesc(RentalStatus.AKTYWNE))
                .thenReturn(activeRentals);

        // when
        List<Rental> result = rentalService.findActiveRentals();

        // then
        assertEquals(1, result.size());
        assertEquals(RentalStatus.AKTYWNE, result.get(0).getStatus());
        verify(rentalRepository, times(1))
                .findByStatusOrderByRentalDateDesc(RentalStatus.AKTYWNE);
    }
    @Test
    void shouldCancelRentalWhenReturnBeforeStartDate() {
        // given
        testRental.setRentalDate(LocalDate.now().plusDays(2)); // Wypożyczenie w przyszłości
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        doNothing().when(carService).markAsAvailable(1L);

        // when
        rentalService.returnCar(1L);

        // then
        assertEquals(RentalStatus.ANULOWANE, testRental.getStatus());
        verify(carService, times(1)).markAsAvailable(1L);
    }
}