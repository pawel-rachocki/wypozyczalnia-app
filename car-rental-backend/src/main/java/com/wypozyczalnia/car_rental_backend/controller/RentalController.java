package com.wypozyczalnia.car_rental_backend.controller;

import com.wypozyczalnia.car_rental_backend.model.dto.RentalRequest;
import com.wypozyczalnia.car_rental_backend.model.entity.Client;
import com.wypozyczalnia.car_rental_backend.model.entity.Rental;
import com.wypozyczalnia.car_rental_backend.model.exception.CarNotFoundException;
import com.wypozyczalnia.car_rental_backend.model.exception.ClientNotFoundException;
import com.wypozyczalnia.car_rental_backend.model.exception.RentalNotFoundException;
import com.wypozyczalnia.car_rental_backend.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/wypozyczenia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    public ResponseEntity<List<Rental>> getAllRentals() {
        List<Rental> wypozyczenia = rentalService.findAll();
        return ResponseEntity.ok(wypozyczenia);
    }
    @GetMapping("/aktywne")
    public ResponseEntity<List<Rental>> getActiveRentals() {
        List<Rental> wypozyczenia = rentalService.findActiveRentals();
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        try {
            Rental rental = rentalService.findById(id);
            return ResponseEntity.ok(rental);
        }catch (RentalNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/wypozycz")
    public ResponseEntity<Rental> rentCar(@Valid @RequestBody RentalRequest request) {
        try {
            Rental rental = rentalService.rentCar(
                    request.getClientId(),
                    request.getCarId(),
                    request.getRentalDate(),
                    request.getPlannedReturnDate()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(rental);
        } catch (IllegalArgumentException | IllegalStateException | ClientNotFoundException | CarNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/zwroc")
    public ResponseEntity<Rental> returnCar(@PathVariable Long id) {
        try {
            Rental rental = rentalService.returnCar(id);
            return ResponseEntity.ok(rental);
        } catch (RentalNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
