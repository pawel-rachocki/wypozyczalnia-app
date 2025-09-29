package com.wypozyczalnia.car_rental_backend.model.entity;

public enum RentalStatus {
    AKTYWNE("Aktywne"),
    ZAKONCZONE("Zakończone"),
    PRZETERMINOWANE("Przeterminowane"),
    ANULOWANE("Anulowane");

    private final String displayName;

    RentalStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
