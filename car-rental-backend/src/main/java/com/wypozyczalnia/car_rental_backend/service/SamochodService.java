package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import com.wypozyczalnia.car_rental_backend.repository.SamochodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SamochodService {

    private final SamochodRepository samochodRepository;

    // ===== OPERACJE CRUD =====

    public List<Samochod> findAll() {
        return samochodRepository.findAll();
    }

    public Optional<Samochod> findById(Long id) {
        return samochodRepository.findById(id);
    }

    @Transactional
    public Samochod save(Samochod samochod) {
        validateSamochod(samochod);

        if (samochod.getStatus() == null) {
            samochod.setStatus(StatusSamochodu.DOSTEPNY);
        }

        Samochod saved = samochodRepository.save(samochod);
        return saved;
    }

    @Transactional
    public Samochod update(Long id, Samochod samochodUpdate) {
        Samochod existing = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        validateSamochod(samochodUpdate);

        existing.setMarka(samochodUpdate.getMarka());
        existing.setModel(samochodUpdate.getModel());
        existing.setCenaZaDzien(samochodUpdate.getCenaZaDzien());
        existing.setStatus(samochodUpdate.getStatus());

        Samochod updated = samochodRepository.save(existing);
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        if (StatusSamochodu.WYPOZYCZONY.equals(samochod.getStatus())) {
            throw new IllegalStateException("Nie można usunąć wypożyczonego samochodu");
        }

        samochodRepository.deleteById(id);
    }

    public List<Samochod> findAvailable() {
        return samochodRepository.findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.DOSTEPNY);
    }

    @Transactional
    public void markAsRented(Long id) {
        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        if (!StatusSamochodu.DOSTEPNY.equals(samochod.getStatus())) {
            throw new IllegalStateException("Samochód nie jest dostępny do wypożyczenia");
        }

        samochod.setStatus(StatusSamochodu.WYPOZYCZONY);
        samochodRepository.save(samochod);
    }

    @Transactional
    public void markAsAvailable(Long id) {
        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        samochod.setStatus(StatusSamochodu.DOSTEPNY);
        samochodRepository.save(samochod);
    }

    public boolean isAvailable(Long id) {
        return samochodRepository.findById(id)
                .map(samochod -> StatusSamochodu.DOSTEPNY.equals(samochod.getStatus()))
                .orElse(false);
    }

    // ===== METODA WALIDACJI DANYCH =====

    private void validateSamochod(Samochod samochod) {
        if (samochod.getMarka() == null || samochod.getMarka().trim().isEmpty()) {
            throw new IllegalArgumentException("Marka samochodu jest wymagana");
        }

        if (samochod.getModel() == null || samochod.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Model samochodu jest wymagany");
        }

        if (samochod.getCenaZaDzien() == null || samochod.getCenaZaDzien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cena za dzień musi być większa od zera");
        }

        if (samochod.getCenaZaDzien().compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Cena za dzień nie może przekraczać 10 000 zł");
        }
    }
}
