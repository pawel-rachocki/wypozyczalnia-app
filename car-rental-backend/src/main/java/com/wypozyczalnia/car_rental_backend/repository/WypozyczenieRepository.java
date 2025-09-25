package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusWypozyczenia;
import com.wypozyczalnia.car_rental_backend.entity.Wypozyczenie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WypozyczenieRepository extends JpaRepository<Wypozyczenie, Long> {

    List<Wypozyczenie> findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia status);

    List<Wypozyczenie> findByKlientOrderByDataWypozyczeniaDesc(Klient klient);

    List<Wypozyczenie> findByKlientIdOrderByDataWypozyczeniaDesc(Long klientId);

    List<Wypozyczenie> findBySamochodOrderByDataWypozyczeniaDesc(Samochod samochod);

    List<Wypozyczenie> findBySamochodIdOrderByDataWypozyczeniaDesc(Long samochodId);

    List<Wypozyczenie> findByKlientIdAndStatus(Long klientId, StatusWypozyczenia status);

    Optional<Wypozyczenie> findBySamochodIdAndStatus(Long samochodId, StatusWypozyczenia status);

    boolean existsBySamochodIdAndStatus(Long samochodId, StatusWypozyczenia status);

    boolean existsByKlientIdAndStatus(Long klientId, StatusWypozyczenia status);

    List<Wypozyczenie> findByDataWypozyczeniaBetweenOrderByDataWypozyczeniaDesc(
            LocalDate startDate, LocalDate endDate);

    @Query("SELECT w FROM Wypozyczenie w WHERE w.status = 'AKTYWNE' AND w.dataZwrotu IS NULL AND w.dataWypozyczenia < :limitDate")
    List<Wypozyczenie> findOverdueRentals(@Param("limitDate") LocalDate limitDate);

    @Query("SELECT w FROM Wypozyczenie w WHERE w.status = 'AKTYWNE' AND w.dataZwrotu = :today")
    List<Wypozyczenie> findRentalsEndingToday(@Param("today") LocalDate today);

    @Query("SELECT SUM(w.kosztCalkowity) FROM Wypozyczenie w WHERE w.status = 'ZAKONCZONE'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(w.kosztCalkowity) FROM Wypozyczenie w WHERE w.status = 'ZAKONCZONE' AND w.dataWypozyczenia BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByStatus(StatusWypozyczenia status);

    @Query("SELECT w.samochod, COUNT(w) as rentCount FROM Wypozyczenie w " +
            "GROUP BY w.samochod ORDER BY rentCount DESC")
    List<Object[]> findMostRentedCars();

    @Query("SELECT w.klient, COUNT(w) as rentCount FROM Wypozyczenie w " +
            "GROUP BY w.klient ORDER BY rentCount DESC")
    List<Object[]> findMostActiveClients();

    @Query("SELECT w FROM Wypozyczenie w WHERE w.status = 'AKTYWNE' AND w.dataZwrotu IS NULL AND w.dataWypozyczenia < :thresholdDate")
    List<Wypozyczenie> findPotentiallyLostRentals(@Param("thresholdDate") LocalDate thresholdDate);
    @Query("SELECT COUNT(w) = 0 FROM Wypozyczenie w WHERE w.klient.id = :klientId AND w.status = 'PRZETERMINOWANE'")
    boolean canKlientRentCar(@Param("klientId") Long klientId);
}
