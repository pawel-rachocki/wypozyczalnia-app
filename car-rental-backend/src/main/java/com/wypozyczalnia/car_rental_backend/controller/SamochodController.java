package com.wypozyczalnia.car_rental_backend.controller;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.service.SamochodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/samochody")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SamochodController {

    private final SamochodService samochodService;

    // ===== PODSTAWOWE OPERACJE CRUD =====

    @GetMapping
    public ResponseEntity<List<Samochod>> getAllSamochody() {
        List<Samochod> samochody = samochodService.findAll();
        return ResponseEntity.ok(samochody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Samochod> getSamochodById(@PathVariable Long id) {
        return samochodService.findById(id)
                .map(samochod -> ResponseEntity.ok(samochod))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Samochod> createSamochod(@Valid @RequestBody Samochod samochod) {
        try {
            Samochod saved = samochodService.save(samochod);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Samochod> updateSamochod(@PathVariable Long id, @Valid @RequestBody Samochod samochod) {
        try {
            Samochod updated = samochodService.update(id, samochod);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSamochod(@PathVariable Long id) {
        try {
            samochodService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ===== OPERACJE BIZNESOWE =====

    @GetMapping("/dostepne")
    public ResponseEntity<List<Samochod>> getAvailableSamochody() {
        List<Samochod> dostepne = samochodService.findAvailable();
        return ResponseEntity.ok(dostepne);
    }

    @GetMapping("/marka/{marka}")
    public ResponseEntity<List<Samochod>> getSamochodyByMarka(@PathVariable String marka) {
        List<Samochod> samochody = samochodService.findByMarka(marka);
        return ResponseEntity.ok(samochody);
    }

    @GetMapping("/szukaj")
    public ResponseEntity<List<Samochod>> searchSamochody(@RequestParam(name = "q") String searchTerm) {
        List<Samochod> samochody = samochodService.search(searchTerm);
        return ResponseEntity.ok(samochody);
    }

    @GetMapping("/cena")
    public ResponseEntity<List<Samochod>> getSamochodyByPriceRange(
            @RequestParam(name = "min", required = false) BigDecimal minPrice,
            @RequestParam(name = "max", required = false) BigDecimal maxPrice) {

        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("10000");

        List<Samochod> samochody = samochodService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(samochody);
    }

    @GetMapping("/dostepne/cena")
    public ResponseEntity<List<Samochod>> getAvailableSamochodyByPriceRange(
            @RequestParam(name = "min", required = false) BigDecimal minPrice,
            @RequestParam(name = "max", required = false) BigDecimal maxPrice) {

        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("10000");

        List<Samochod> samochody = samochodService.findAvailableByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(samochody);
    }

    // ===== OPERACJE STATUSU =====

    @PatchMapping("/{id}/wypozycz")
    public ResponseEntity<Void> markAsRented(@PathVariable Long id) {
        try {
            samochodService.markAsRented(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{id}/zwroc")
    public ResponseEntity<Void> markAsAvailable(@PathVariable Long id) {
        try {
            samochodService.markAsAvailable(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/dostepnosc")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long id) {
        boolean available = samochodService.isAvailable(id);
        return ResponseEntity.ok(available);
    }
}
