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

    @GetMapping("/email/{email}")
    public ResponseEntity<Klient> getKlientByEmail(@PathVariable String email) {
        return klientService.findByEmail(email)
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

    // ===== OPERACJE WYSZUKIWANIA =====

    @GetMapping("/szukaj")
    public ResponseEntity<List<Klient>> searchKlienci(@RequestParam(name = "q") String searchTerm) {
        List<Klient> klienci = klientService.search(searchTerm);
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/imie/{imie}")
    public ResponseEntity<List<Klient>> getKlienciByImie(@PathVariable String imie) {
        List<Klient> klienci = klientService.findByImie(imie);
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/nazwisko/{nazwisko}")
    public ResponseEntity<List<Klient>> getKlienciByNazwisko(@PathVariable String nazwisko) {
        List<Klient> klienci = klientService.findByNazwisko(nazwisko);
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/domena/{domena}")
    public ResponseEntity<List<Klient>> getKlienciByEmailDomain(@PathVariable String domena) {
        List<Klient> klienci = klientService.findByEmailDomain(domena);
        return ResponseEntity.ok(klienci);
    }

    // ===== OPERACJE BIZNESOWE =====

    @GetMapping("/aktywni")
    public ResponseEntity<List<Klient>> getKlienciWithActiveRentals() {
        List<Klient> klienci = klientService.findKlientsWithActiveRentals();
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/bez-wypozyczen")
    public ResponseEntity<List<Klient>> getKlienciWithoutRentals() {
        List<Klient> klienci = klientService.findKlientsWithoutRentals();
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/najaktywniejszi")
    public ResponseEntity<List<Klient>> getMostActiveKlienci() {
        List<Klient> klienci = klientService.findMostActiveKlients();
        return ResponseEntity.ok(klienci);
    }

    @GetMapping("/{id}/aktywne-wypozyczenia")
    public ResponseEntity<Boolean> hasActiveRentals(@PathVariable Long id) {
        boolean hasActive = klientService.hasActiveRentals(id);
        return ResponseEntity.ok(hasActive);
    }

    @GetMapping("/{id}/liczba-wypozyczen")
    public ResponseEntity<Long> countKlientRentals(@PathVariable Long id) {
        long count = klientService.countRentals(id);
        return ResponseEntity.ok(count);
    }

    // ===== WALIDACJA =====

    @GetMapping("/email-exists/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = klientService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

}