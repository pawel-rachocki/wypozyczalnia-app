package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SamochodRepository extends JpaRepository<Samochod,Long> {

    List<Samochod> findByStatus(StatusSamochodu status);

    List<Samochod> findByStatusOrderByMarkaAscModelAsc(StatusSamochodu status);

    List<Samochod> findByMarkaIgnoreCaseOrderByModelAsc(String marka);

    Optional<Samochod> findByMarkaIgnoreCaseAndModelIgnoreCase(String marka, String model);

    List<Samochod> findByCenaZaDzienBetweenOrderByCenaZaDzienAsc(BigDecimal minCena, BigDecimal maxCena);

    List<Samochod> findByStatusAndCenaZaDzienBetweenOrderByCenaZaDzienAsc(
            StatusSamochodu status, BigDecimal minCena, BigDecimal maxCena);

    List<Samochod> findByMarkaIgnoreCaseAndStatusOrderByModelAsc(String marka, StatusSamochodu status);

    boolean existsByMarkaIgnoreCaseAndModelIgnoreCase(String marka, String model);

    long countByStatus(StatusSamochodu status);

    @Query("SELECT s FROM Samochod s WHERE s.status = :status ORDER BY s.cenaZaDzien ASC")
    List<Samochod> findCheapestAvailableCars(@Param("status") StatusSamochodu status);

    @Query("SELECT s FROM Samochod s WHERE " +
            "LOWER(s.marka) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY s.marka ASC, s.model ASC")
    List<Samochod> findByMarkaOrModelContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT AVG(s.cenaZaDzien) FROM Samochod s WHERE s.status = :status")
    BigDecimal findAveragePriceByStatus(@Param("status") StatusSamochodu status);
}
