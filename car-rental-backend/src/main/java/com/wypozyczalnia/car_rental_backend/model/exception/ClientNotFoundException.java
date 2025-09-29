package com.wypozyczalnia.car_rental_backend.model.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long clientId) {
        super("Client with id " + clientId + " not found.");
    }

}
