package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.*;
import com.wypozyczalnia.car_rental_backend.model.exception.RentalNotFoundException;
import com.wypozyczalnia.car_rental_backend.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final ClientService clientService;

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RentalNotFoundException(id));
    }

    @Transactional
    public Rental rentCar(Long klientId, Long samochodId, LocalDate dataWypozyczenia, LocalDate planowanaDataZwrotu) {
        validateRentalData(klientId, samochodId, dataWypozyczenia, planowanaDataZwrotu);

        Client client = clientService.findById(klientId);

        Car car = carService.findById(samochodId);

        if (rentalRepository.existsByCarIdAndStatus(samochodId, RentalStatus.AKTYWNE)) {
            throw new IllegalStateException("Car already rented");
        }

        int liczbaDni = Period.between(dataWypozyczenia, planowanaDataZwrotu).getDays();
        if (liczbaDni == 0) { // W przypadku wypożyczenia i zwrotu tego samego dnia, naliczamy 1 dzień
            liczbaDni = 1;
        }

        BigDecimal totalCost = car.getDailyPrice()
                .multiply(BigDecimal.valueOf(liczbaDni));

        Rental rental = new Rental(
                client, car, dataWypozyczenia, totalCost, planowanaDataZwrotu
        );

        Rental savedRental = rentalRepository.save(rental);

        carService.markAsRented(samochodId);

        return savedRental;
    }

    public List<Rental> findByStatus(RentalStatus status) {
        return rentalRepository.findByStatusOrderByRentalDateDesc(status);
    }

    public List<Rental> findActiveRentals() {
        return findByStatus(RentalStatus.AKTYWNE);
    }

    @Transactional
    public Rental returnCar(Long wypozyczenieId) {
        LocalDate dataZwrotu = LocalDate.now();

        Rental rental = rentalRepository.findById(wypozyczenieId)
                .orElseThrow(() -> new RentalNotFoundException(wypozyczenieId));

        if (!RentalStatus.AKTYWNE.equals(rental.getStatus())) {
            throw new IllegalStateException("Rental is not active");
        }

        if (dataZwrotu.isBefore(rental.getRentalDate())) {
            rental.setStatus(RentalStatus.ANULOWANE);
            rental.setReturnDate(dataZwrotu);
            rental.setTotalCost(BigDecimal.ZERO);
        } else {
            rental.setReturnDate(dataZwrotu);
            rental.setStatus(RentalStatus.ZAKONCZONE);
        }

        Rental updated = rentalRepository.save(rental);
        carService.markAsAvailable(rental.getCar().getId());

        return updated;
    }

    private void validateRentalData(Long clientId, Long carId, LocalDate rentalDate, LocalDate plannedReturnDate) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }

        if (carId == null) {
            throw new IllegalArgumentException("Car ID is required");
        }

        if (rentalDate == null) {
            throw new IllegalArgumentException("Rental date is required");
        }

        if (plannedReturnDate == null) {
            throw new IllegalArgumentException("Planned return date is required");
        }

        if (rentalDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Rental date cannot be in the past");
        }

        if (plannedReturnDate.isBefore(rentalDate) || plannedReturnDate.isEqual(rentalDate)) {
            throw new IllegalArgumentException("Return date must be later than rental date");
        }

    }
}
