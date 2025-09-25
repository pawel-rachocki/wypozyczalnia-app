package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import com.wypozyczalnia.car_rental_backend.repository.SamochodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SamochodService {

    private final SamochodRepository samochodRepository;

    // ===== OPERACJE CRUD =====

    public List<Samochod> findAll() {
        log.debug("Pobieranie wszystkich samochodów");
        return samochodRepository.findAll();
    }

    public Optional<Samochod> findById(Long id) {
        log.debug("Pobieranie samochodu o ID: {}", id);
        return samochodRepository.findById(id);
    }

    @Transactional
    public Samochod save(Samochod samochod) {
        log.info("Zapisywanie nowego samochodu: {} {}", samochod.getMarka(), samochod.getModel());

        validateSamochod(samochod);

        if (samochodRepository.existsByMarkaIgnoreCaseAndModelIgnoreCase(
                samochod.getMarka(), samochod.getModel())) {
            throw new IllegalArgumentException(
                    String.format("Samochód %s %s już istnieje w systemie",
                            samochod.getMarka(), samochod.getModel()));
        }

        if (samochod.getStatus() == null) {
            samochod.setStatus(StatusSamochodu.DOSTEPNY);
        }

        Samochod saved = samochodRepository.save(samochod);
        log.info("Zapisano samochód z ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Samochod update(Long id, Samochod samochodUpdate) {
        log.info("Aktualizacja samochodu o ID: {}", id);

        Samochod existing = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        validateSamochod(samochodUpdate);

        if (!existing.getMarka().equalsIgnoreCase(samochodUpdate.getMarka()) ||
                !existing.getModel().equalsIgnoreCase(samochodUpdate.getModel())) {

            if (samochodRepository.existsByMarkaIgnoreCaseAndModelIgnoreCase(
                    samochodUpdate.getMarka(), samochodUpdate.getModel())) {
                throw new IllegalArgumentException(
                        String.format("Samochód %s %s już istnieje w systemie",
                                samochodUpdate.getMarka(), samochodUpdate.getModel()));
            }
        }

        existing.setMarka(samochodUpdate.getMarka());
        existing.setModel(samochodUpdate.getModel());
        existing.setCenaZaDzien(samochodUpdate.getCenaZaDzien());
        existing.setStatus(samochodUpdate.getStatus());

        Samochod updated = samochodRepository.save(existing);
        log.info("Zaktualizowano samochód: {} {}", updated.getMarka(), updated.getModel());
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Usuwanie samochodu o ID: {}", id);

        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        if (StatusSamochodu.WYPOZYCZONY.equals(samochod.getStatus())) {
            throw new IllegalStateException("Nie można usunąć wypożyczonego samochodu");
        }

        samochodRepository.deleteById(id);
        log.info("Usunięto samochód: {} {}", samochod.getMarka(), samochod.getModel());
    }

    public List<Samochod> findAvailable() {
        log.debug("Pobieranie dostępnych samochodów");
        return samochodRepository.findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.DOSTEPNY);
    }

    public List<Samochod> findByMarka(String marka) {
        log.debug("Pobieranie samochodów marki: {}", marka);
        return samochodRepository.findByMarkaIgnoreCaseOrderByModelAsc(marka);
    }

    public List<Samochod> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Pobieranie samochodów w przedziale cenowym: {} - {}", minPrice, maxPrice);
        return samochodRepository.findByCenaZaDzienBetweenOrderByCenaZaDzienAsc(minPrice, maxPrice);
    }

    public List<Samochod> findAvailableByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Pobieranie dostępnych samochodów w przedziale cenowym: {} - {}", minPrice, maxPrice);
        return samochodRepository.findByStatusAndCenaZaDzienBetweenOrderByCenaZaDzienAsc(
                StatusSamochodu.DOSTEPNY, minPrice, maxPrice);
    }

    public List<Samochod> search(String searchTerm) {
        log.debug("Wyszukiwanie samochodów po frazje: {}", searchTerm);
        return samochodRepository.findByMarkaOrModelContainingIgnoreCase(searchTerm);
    }

    @Transactional
    public void markAsRented(Long id) {
        log.info("Oznaczanie samochodu o ID {} jako wypożyczony", id);

        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        if (!StatusSamochodu.DOSTEPNY.equals(samochod.getStatus())) {
            throw new IllegalStateException("Samochód nie jest dostępny do wypożyczenia");
        }

        samochod.setStatus(StatusSamochodu.WYPOZYCZONY);
        samochodRepository.save(samochod);
        log.info("Samochód {} {} oznaczony jako wypożyczony", samochod.getMarka(), samochod.getModel());
    }

    @Transactional
    public void markAsAvailable(Long id) {
        log.info("Oznaczanie samochodu o ID {} jako dostępny", id);

        Samochod samochod = samochodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + id + " nie istnieje"));

        samochod.setStatus(StatusSamochodu.DOSTEPNY);
        samochodRepository.save(samochod);
        log.info("Samochód {} {} oznaczony jako dostępny", samochod.getMarka(), samochod.getModel());
    }

    public boolean isAvailable(Long id) {
        return samochodRepository.findById(id)
                .map(samochod -> StatusSamochodu.DOSTEPNY.equals(samochod.getStatus()))
                .orElse(false);
    }

    public long countByStatus(StatusSamochodu status) {
        return samochodRepository.countByStatus(status);
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
