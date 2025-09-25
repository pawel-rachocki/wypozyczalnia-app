package com.wypozyczalnia.car_rental_backend.controller;

import com.wypozyczalnia.car_rental_backend.dto.WypozyczenieRequest;
import com.wypozyczalnia.car_rental_backend.entity.StatusWypozyczenia;
import com.wypozyczalnia.car_rental_backend.entity.Wypozyczenie;
import com.wypozyczalnia.car_rental_backend.service.WypozyczenieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wypozyczenia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WypozyczenieController {

    private final WypozyczenieService wypozyczenieService;

    // ===== PODSTAWOWE OPERACJE =====

    @GetMapping
    public ResponseEntity<List<Wypozyczenie>> getAllWypozyczenia() {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findAll();
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wypozyczenie> getWypozyczenieById(@PathVariable Long id) {
        return wypozyczenieService.findById(id)
                .map(wypozyczenie -> ResponseEntity.ok(wypozyczenie))
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== OPERACJE WYPOŻYCZANIA =====

    @PostMapping("/wypozycz")
    public ResponseEntity<Wypozyczenie> rentCar(@Valid @RequestBody WypozyczenieRequest request) {
        try {
            Wypozyczenie wypozyczenie = wypozyczenieService.rentCar(
                    request.getKlientId(),
                    request.getSamochodId(),
                    request.getDataWypozyczenia(),
                    request.getPlanowanaDataZwrotu()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(wypozyczenie);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/zwroc")
    public ResponseEntity<Wypozyczenie> returnCar(@PathVariable Long id,
                                                  @RequestParam LocalDate dataZwrotu) {
        try {
            Wypozyczenie wypozyczenie = wypozyczenieService.returnCar(id, dataZwrotu);
            return ResponseEntity.ok(wypozyczenie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== WYSZUKIWANIE =====

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Wypozyczenie>> getWypozyczenieByStatus(@PathVariable StatusWypozyczenia status) {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findByStatus(status);
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/klient/{klientId}")
    public ResponseEntity<List<Wypozyczenie>> getWypozyczenieByKlient(@PathVariable Long klientId) {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findByKlient(klientId);
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/samochod/{samochodId}")
    public ResponseEntity<List<Wypozyczenie>> getWypozyczenieBySamochod(@PathVariable Long samochodId) {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findBySamochod(samochodId);
        return ResponseEntity.ok(wypozyczenia);
    }

    // ===== OPERACJE BIZNESOWE =====

    @GetMapping("/aktywne")
    public ResponseEntity<List<Wypozyczenie>> getActiveRentals() {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findActiveRentals();
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/przeterminowane")
    public ResponseEntity<List<Wypozyczenie>> getOverdueRentals() {
        List<Wypozyczenie> wypozyczenia = wypozyczenieService.findOverdueRentals();
        return ResponseEntity.ok(wypozyczenia);
    }

    @GetMapping("/przychod")
    public ResponseEntity<BigDecimal> getRevenueInPeriod(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        BigDecimal revenue = wypozyczenieService.calculateRevenueInPeriod(from, to);
        return ResponseEntity.ok(revenue);
    }
}
