package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KlientRepository extends JpaRepository<Klient, Long> {

    Optional<Klient> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Klient> findByImieIgnoreCaseOrderByNazwiskoAsc(String imie);

    List<Klient> findByNazwiskoIgnoreCaseOrderByImieAsc(String nazwisko);

    List<Klient> findByImieIgnoreCaseAndNazwiskoIgnoreCaseOrderByEmailAsc(String imie, String nazwisko);

    @Query("SELECT k FROM Klient k WHERE " +
            "LOWER(k.imie) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(k.nazwisko) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY k.nazwisko ASC, k.imie ASC")
    List<Klient> findByImieOrNazwiskoContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    List<Klient> findByEmailContainingIgnoreCaseOrderByEmailAsc(String emailPart);

    @Query("SELECT DISTINCT k FROM Klient k JOIN k.wypozyczenia w WHERE w.status = 'AKTYWNE'")
    List<Klient> findKlientsWithActiveRentals();

    @Query("SELECT k FROM Klient k WHERE k.wypozyczenia IS EMPTY ORDER BY k.nazwisko ASC")
    List<Klient> findKlientsWithoutRentals();

    @Query("SELECT COUNT(w) FROM Wypozyczenie w WHERE w.klient.id = :klientId")
    long countRentalsByKlientId(@Param("klientId") Long klientId);

    @Query("SELECT COUNT(w) > 0 FROM Wypozyczenie w WHERE w.klient.id = :klientId AND w.status = 'AKTYWNE'")
    boolean hasActiveRentals(@Param("klientId") Long klientId);

    @Query("SELECT k FROM Klient k LEFT JOIN k.wypozyczenia w " +
            "GROUP BY k.id ORDER BY COUNT(w) DESC")
    List<Klient> findMostActiveKlients();
}
