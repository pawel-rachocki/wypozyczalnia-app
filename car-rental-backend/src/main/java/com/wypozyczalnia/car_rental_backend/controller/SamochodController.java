package com.wypozyczalnia.car_rental_backend.controller;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.service.SamochodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
}
