package com.wypozyczalnia.car_rental_backend.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Rental date is required")
    @FutureOrPresent(message = "Rental date cannot be in the past")
    private LocalDate rentalDate;

    @NotNull(message = "Planned return date is required")
    @Future(message = "Planned return date must be in the future")
    private LocalDate plannedReturnDate;
}
