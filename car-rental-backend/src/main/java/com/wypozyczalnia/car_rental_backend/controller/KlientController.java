package com.wypozyczalnia.car_rental_backend.controller;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import com.wypozyczalnia.car_rental_backend.service.KlientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/klienci")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KlientController {

    private final KlientService klientService;

    // ===== PODSTAWOWE OPERACJE CRUD =====

    @GetMapping
    public ResponseEntity<List<Klient>> getAllKlienci() {
        List<Klient> klienci = klientService.findAll();
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Klient> getKlientById(@PathVariable Long id) {
        return klientService.findById(id)
                .map(klient -> ResponseEntity.ok(klient))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Klient> createKlient(@Valid @RequestBody Klient klient) {
        try {
            Klient saved = klientService.save(klient);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Klient> updateKlient(@PathVariable Long id,
                                               @Valid @RequestBody Klient klient) {
        try {
            Klient updated = klientService.update(id, klient);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKlient(@PathVariable Long id) {
        try {
            klientService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}