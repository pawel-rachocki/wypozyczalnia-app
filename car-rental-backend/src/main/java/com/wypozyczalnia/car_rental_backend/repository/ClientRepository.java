package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByEmail(String email);
    Optional<Client> findByEmail(String email);

    @Query("SELECT COUNT(w) > 0 FROM Rental w WHERE w.client.id = :clientId AND w.status = 'AKTYWNE'")
    boolean hasActiveRentals(@Param("clientId") Long clientId);
}
