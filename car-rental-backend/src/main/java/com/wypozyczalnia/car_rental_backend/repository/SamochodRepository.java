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
    List<Samochod> findByStatusOrderByMarkaAscModelAsc(StatusSamochodu status);

    boolean existsByMarkaIgnoreCaseAndModelIgnoreCase(String marka, String model);

}
