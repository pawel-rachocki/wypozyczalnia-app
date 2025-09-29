package com.wypozyczalnia.car_rental_backend.model.exception;

public class CarNotFoundException extends RuntimeException {

    public CarNotFoundException(Long carId) {
        super("Car with id " + carId + " not found.");
    }

}
