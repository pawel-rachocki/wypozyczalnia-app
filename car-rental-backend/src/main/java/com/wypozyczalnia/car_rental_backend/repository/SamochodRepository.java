package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SamochodRepository extends JpaRepository<Samochod,Long> {
    List<Samochod> findByStatusOrderByMarkaAscModelAsc(StatusSamochodu status);
    List<Samochod> findByMarkaIgnoreCaseOrderByModelAsc(String marka);

    boolean existsByMarkaIgnoreCaseAndModelIgnoreCase(String marka, String model);

}
