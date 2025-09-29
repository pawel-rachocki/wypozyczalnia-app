package com.wypozyczalnia.car_rental_backend.model.entity;

public enum StatusWypozyczenia {
    AKTYWNE("Aktywne"),
    ZAKONCZONE("Zakończone"),
    PRZETERMINOWANE("Przeterminowane"),
    ANULOWANE("Anulowane");

    private final String displayName;

    StatusWypozyczenia(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
