package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.RentalStatus;
import com.wypozyczalnia.car_rental_backend.model.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    boolean existsByCarIdAndStatus(Long samochodId, RentalStatus status);
    List<Rental> findByClientIdOrderByRentalDateDesc(Long klientId);
    List<Rental> findByStatusOrderByRentalDateDesc(RentalStatus status);
}
