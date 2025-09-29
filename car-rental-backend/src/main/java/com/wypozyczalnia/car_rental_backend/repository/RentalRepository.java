package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.StatusWypozyczenia;
import com.wypozyczalnia.car_rental_backend.model.entity.Wypozyczenie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WypozyczenieRepository extends JpaRepository<Wypozyczenie, Long> {
    boolean existsBySamochodIdAndStatus(Long samochodId, StatusWypozyczenia status);
    List<Wypozyczenie> findByKlientIdOrderByDataWypozyczeniaDesc(Long klientId);
    List<Wypozyczenie> findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia status);
}
