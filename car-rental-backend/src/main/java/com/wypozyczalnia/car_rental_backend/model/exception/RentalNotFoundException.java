package com.wypozyczalnia.car_rental_backend.model.exception;

public class RentalNotFoundException extends RuntimeException {

    public RentalNotFoundException(Long rentalId) {
        super("Rental with id " + rentalId + " not found.");
    }

}
