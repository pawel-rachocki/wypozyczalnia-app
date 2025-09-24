package com.wypozyczalnia.car_rental_backend.entity;

public enum StatusSamochodu {
    DOSTEPNY("Dostępny"),
    WYPOZYCZONY("Wypożyczony");

    private final String displayName;

    StatusSamochodu(String displayName){
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
