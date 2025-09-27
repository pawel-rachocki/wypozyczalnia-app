package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KlientRepository extends JpaRepository<Klient, Long> {
    boolean existsByEmail(String email);
    Optional<Klient> findByEmail(String email);

    @Query("SELECT COUNT(w) > 0 FROM Wypozyczenie w WHERE w.klient.id = :klientId AND w.status = 'AKTYWNE'")
    boolean hasActiveRentals(@Param("klientId") Long klientId);
}
