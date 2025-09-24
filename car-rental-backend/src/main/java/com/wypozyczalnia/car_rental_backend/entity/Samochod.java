package com.wypozyczalnia.car_rental_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "samochody")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Samochod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Atrybut (marka) jest wymagany")
    @Column(nullable = false,length = 50)
    private String marka;

    @NotBlank(message = "Atrybut (model) jest wymagany")
    @Column(nullable = false,length = 50)
    private String model;

    @NotBlank(message = "Atrybut (cena za dzień) jest wymagany")
    @Positive(message = "Cena za dzień musi być większa od zera")
    @Column(name = "cena_za_dzien", nullable = false, precision = 10, scale = 2)
    private BigDecimal cenaZaDzien;

    @NotBlank(message = "Atrybut (status) jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private StatusSamochodu status;

    @OneToMany(mappedBy = "samochod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wypozyczenie> wypozyczenia = new ArrayList<>();

    public Samochod(String marka, String model, BigDecimal cenaZaDzien, StatusSamochodu status) {
        this.marka = marka;
        this.model = model;
        this.cenaZaDzien = cenaZaDzien;
        this.status = status;
    }

    public String getFullName() {
        return marka + " " + model;
    }

    public boolean isAvailable() {
        return StatusSamochodu.DOSTEPNY.equals(status);
    }
}
