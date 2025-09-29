package com.wypozyczalnia.car_rental_backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "wypozyczenia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wypozyczenie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Atrybut (klient) jest wymagany")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id", nullable = false)
    private Klient klient;

    @NotNull(message = "Atrybut (samochod) jest wymagany")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "samochod_id", nullable = false)
    private Car car;

    @NotNull(message = "Atrybut (data wypożyczenia) jest wymagany")
    @Column(name = "data_wypozyczenia", nullable = false)
    private LocalDate dataWypozyczenia;

    @NotNull(message = "Atrybut (data zwrotu) jest wymagany")
    @Column(name = "data_zwrotu")
    private LocalDate dataZwrotu;

    @NotNull(message = "Atrybut (koszt całkowity) jest wymagany")
    @Column(name = "koszt_calkowity", nullable = false, precision = 10, scale = 2)
    private BigDecimal kosztCalkowity;

    @NotNull(message = "Atrybut (status wypożyczenia) jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusWypozyczenia status;

    public Wypozyczenie(Klient klient, Car car, LocalDate dataWypozyczenia, BigDecimal kosztCalkowity, LocalDate dataZwrotu) {
        this.klient = klient;
        this.car = car;
        this.dataWypozyczenia = dataWypozyczenia;
        this.kosztCalkowity = kosztCalkowity;
        this.dataZwrotu = dataZwrotu;
        this.status = StatusWypozyczenia.AKTYWNE;
    }

    public int getLiczbaDni(){
        LocalDate koniecData = (dataZwrotu != null) ? dataZwrotu : LocalDate.now();
        return Period.between(dataWypozyczenia,koniecData).getDays();
    }

    public boolean isActive(){
        return StatusWypozyczenia.AKTYWNE.equals(this.status);
    }

    @Override
    public String toString() {
        return "Wypozyczenie{" +
                "id=" + id +
                ", klient=" + klient +
                ", samochod=" + car +
                ", dataWypozyczenia=" + dataWypozyczenia +
                ", dataZwrotu=" + dataZwrotu +
                ", kosztCalkowity=" + kosztCalkowity +
                ", status=" + status +
                '}';
    }
}
