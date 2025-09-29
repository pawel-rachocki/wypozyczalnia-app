package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RentalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RentalRepository rentalRepository;

    private Client testClient;
    private Car testCar;
    private Rental availableRental;
    private Rental finishedRental;

    @BeforeEach
    void setUp() {
        // Klient
        testClient = new Client();
        testClient.setFirstName("Jan");
        testClient.setLastName("Kowalski");
        testClient.setEmail("jan.kowalski@email.com");
        entityManager.persistAndFlush(testClient);

        // Samochód
        testCar = new Car();
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setDailyPrice(BigDecimal.valueOf(100.00));
        testCar.setStatus(CarStatus.WYPOZYCZONY);
        entityManager.persistAndFlush(testCar);

        // Aktywne wypożyczenie
        availableRental = new Rental();
        availableRental.setClient(testClient);
        availableRental.setCar(testCar);
        availableRental.setRentalDate(LocalDate.now().minusDays(2));
        availableRental.setTotalCost(BigDecimal.valueOf(300.00));
        availableRental.setStatus(RentalStatus.AKTYWNE);
        availableRental.setReturnDate(LocalDate.now().plusDays(2));
        entityManager.persistAndFlush(availableRental);

        // Zakończone wypożyczenie
        finishedRental = new Rental();
        finishedRental.setClient(testClient);
        finishedRental.setCar(testCar);
        finishedRental.setRentalDate(LocalDate.now().minusDays(10));
        finishedRental.setReturnDate(LocalDate.now().minusDays(7));
        finishedRental.setTotalCost(BigDecimal.valueOf(300.00));
        finishedRental.setStatus(RentalStatus.ZAKONCZONE);
        entityManager.persistAndFlush(finishedRental);
    }

    @Test
    void shouldFindActiveRentalsSortedByDate() {
        // given - dane w setUp()

        // when
        List<Rental> result = rentalRepository.findByStatusOrderByRentalDateDesc(RentalStatus.AKTYWNE);

        // then
        assertEquals(1, result.size());
        assertEquals(RentalStatus.AKTYWNE, result.get(0).getStatus());
        assertEquals(testClient, result.get(0).getClient());
        assertEquals(testCar, result.get(0).getCar());
    }

    @Test
    void shouldFindCompletedRentals() {
        // given - dane w setUp()

        // when
        List<Rental> result = rentalRepository.findByStatusOrderByRentalDateDesc(RentalStatus.ZAKONCZONE);

        // then
        assertEquals(1, result.size());
        assertEquals(RentalStatus.ZAKONCZONE, result.get(0).getStatus());
        assertNotNull(result.get(0).getReturnDate());
    }

    @Test
    void shouldFindRentalsByKlientId() {
        // given - dane w setUp()

        // when
        List<Rental> result = rentalRepository.findByClientIdOrderByRentalDateDesc(testClient.getId());

        // then
        assertEquals(2, result.size()); // Aktywne + zakończone
        result.forEach(wypozyczenie -> assertEquals(testClient.getId(), wypozyczenie.getClient().getId()));

        // najnowsze w pierwszej kolejności
        assertTrue(result.get(0).getRentalDate().isAfter(result.get(1).getRentalDate()) ||
                result.get(0).getRentalDate().isEqual(result.get(1).getRentalDate()));
    }

    @Test
    void shouldReturnTrueWhenActiveRentalExistsForCar() {
        // given - dane w setUp()

        // when
        boolean exists = rentalRepository.existsByCarIdAndStatus(testCar.getId(), RentalStatus.AKTYWNE);

        // then
        assertTrue(exists);
    }

}