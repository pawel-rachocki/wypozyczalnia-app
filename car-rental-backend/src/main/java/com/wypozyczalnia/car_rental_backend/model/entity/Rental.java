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
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Client is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "Car is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull(message = "Rental date is required")
    @Column(name = "rental_date", nullable = false)
    private LocalDate rentalDate;

    @NotNull(message = "Return date is required")
    @Column(name = "return_date")
    private LocalDate returnDate;

    @NotNull(message = "Total cost is required")
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @NotNull(message = "Rental status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status;

    public Rental(Client client, Car car, LocalDate rentalDate, BigDecimal totalCost, LocalDate returnDate) {
        this.client = client;
        this.car = car;
        this.rentalDate = rentalDate;
        this.totalCost = totalCost;
        this.returnDate = returnDate;
        this.status = RentalStatus.AKTYWNE;
    }

    @Override
    public String toString() {
        return "Rental{" +
                "id=" + id +
                ", client=" + client +
                ", car=" + car +
                ", rentalDate=" + rentalDate +
                ", returnDate=" + returnDate +
                ", totalCost=" + totalCost +
                ", status=" + status +
                '}';
    }
}
