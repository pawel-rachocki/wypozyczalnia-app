package com.wypozyczalnia.car_rental_backend.model.entity;

public enum CarStatus {
    DOSTEPNY("Dostępny"),
    WYPOZYCZONY("Wypożyczony");

    private final String displayName;

    CarStatus(String displayName){
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return this.displayName;
    }
}