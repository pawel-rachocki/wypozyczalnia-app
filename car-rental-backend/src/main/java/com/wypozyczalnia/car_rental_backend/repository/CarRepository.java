package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.Car;
import com.wypozyczalnia.car_rental_backend.model.entity.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car,Long> {
    List<Car> findByStatusOrderByBrandAscModelAsc(CarStatus status);
    List<Car> findByBrandIgnoreCaseOrderByModelAsc(String marka);

    boolean existsByBrandIgnoreCaseAndModelIgnoreCase(String marka, String model);

}
