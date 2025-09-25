package com.wypozyczalnia.car_rental_backend.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "klienci")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Klient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Atrybut (imię) jest wymagany")
    @Size(min = 2, max = 50, message = "Imię musi zawierać od 2 do 50 znaków")
    @Column(nullable = false,length = 50)
    private String imie;

    @NotBlank(message = "Atrybut (nazwisko) jest wymagany")
    @Size(min = 2, max = 50, message = "Nazwisko musi zawierać od 2 do 50 znaków")
    @Column(nullable = false,length = 50)
    private String nazwisko;

    @NotBlank(message = "Atrybut (email) jest wymagany")
    @Email(message = "Email musi mieć prawidłowy format")
    @Column(nullable = false,unique = true,length = 255)
    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "klient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wypozyczenie> wypozyczenia = new ArrayList<>();

    public Klient(String imie, String nazwisko, String email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.email = email;
    }

    public String getFullName() {
        return imie + " " + nazwisko;
    }

    @Override
    public String toString() {
        return "Klient{" +
                "id=" + id +
                ", imie='" + imie + '\'' +
                ", nazwisko='" + nazwisko + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
