package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.StatusWypozyczenia;
import com.wypozyczalnia.car_rental_backend.entity.Wypozyczenie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WypozyczenieRepository extends JpaRepository<Wypozyczenie, Long> {

    boolean existsBySamochodIdAndStatus(Long samochodId, StatusWypozyczenia status);
    List<Wypozyczenie> findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia status);
}
