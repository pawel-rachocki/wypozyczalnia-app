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
public class WypozyczenieRequest {

    @NotNull(message = "ID klienta jest wymagane")
    private Long klientId;

    @NotNull(message = "ID samochodu jest wymagane")
    private Long samochodId;

    @NotNull(message = "Data wypożyczenia jest wymagana")
    @FutureOrPresent(message = "Data wypożyczenia nie może być w przeszłości")
    private LocalDate dataWypozyczenia;

    @NotNull(message = "Planowana data zwrotu jest wymagana")
    @Future(message = "Planowana data zwrotu musi być w przyszłości")
    private LocalDate planowanaDataZwrotu;
}
