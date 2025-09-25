package com.wypozyczalnia.car_rental_backend.service;
import com.wypozyczalnia.car_rental_backend.entity.Klient;
import com.wypozyczalnia.car_rental_backend.repository.KlientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KlientService {

    private final KlientRepository klientRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    // ===== OPERACJE CRUD =====

    public List<Klient> findAll() {
        log.debug("Pobieranie wszystkich klientów");
        return klientRepository.findAll();
    }

    public Optional<Klient> findById(Long id) {
        log.debug("Pobieranie klienta o ID: {}", id);
        return klientRepository.findById(id);
    }

    public Optional<Klient> findByEmail(String email) {
        log.debug("Pobieranie klienta po emailu: {}", email);
        return klientRepository.findByEmail(email);
    }

    @Transactional
    public Klient save(Klient klient) {
        log.info("Zapisywanie nowego klienta: {} {}, email: {}",
                klient.getImie(), klient.getNazwisko(), klient.getEmail());

        validateKlient(klient);

        if (klientRepository.existsByEmail(klient.getEmail())) {
            throw new IllegalArgumentException(
                    String.format("Klient z emailem %s już istnieje w systemie", klient.getEmail()));
        }

        klient.setImie(normalizeText(klient.getImie()));
        klient.setNazwisko(normalizeText(klient.getNazwisko()));
        klient.setEmail(klient.getEmail().toLowerCase().trim());

        Klient saved = klientRepository.save(klient);
        log.info("Zapisano klienta z ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Klient update(Long id, Klient klientUpdate) {
        log.info("Aktualizacja klienta o ID: {}", id);

        Klient existing = klientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Klient o ID " + id + " nie istnieje"));

        validateKlient(klientUpdate);

        if (!existing.getEmail().equalsIgnoreCase(klientUpdate.getEmail())) {
            if (klientRepository.existsByEmail(klientUpdate.getEmail())) {
                throw new IllegalArgumentException(
                        String.format("Klient z emailem %s już istnieje w systemie", klientUpdate.getEmail()));
            }
        }

        existing.setImie(normalizeText(klientUpdate.getImie()));
        existing.setNazwisko(normalizeText(klientUpdate.getNazwisko()));
        existing.setEmail(klientUpdate.getEmail().toLowerCase().trim());

        Klient updated = klientRepository.save(existing);
        log.info("Zaktualizowano klienta: {} {}", updated.getImie(), updated.getNazwisko());
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Usuwanie klienta o ID: {}", id);

        Klient klient = klientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Klient o ID " + id + " nie istnieje"));

        if (klientRepository.hasActiveRentals(id)) {
            throw new IllegalStateException("Nie można usunąć klienta z aktywnymi wypożyczeniami");
        }

        klientRepository.deleteById(id);
        log.info("Usunięto klienta: {} {}", klient.getImie(), klient.getNazwisko());
    }

    // ===== OPERACJE BIZNESOWE =====

    public List<Klient> search(String searchTerm) {
        log.debug("Wyszukiwanie klientów po frazie: {}", searchTerm);
        return klientRepository.findByImieOrNazwiskoContainingIgnoreCase(searchTerm);
    }

    public List<Klient> findByImie(String imie) {
        log.debug("Pobieranie klientów o imieniu: {}", imie);
        return klientRepository.findByImieIgnoreCaseOrderByNazwiskoAsc(imie);
    }

    public List<Klient> findByNazwisko(String nazwisko) {
        log.debug("Pobieranie klientów o nazwisku: {}", nazwisko);
        return klientRepository.findByNazwiskoIgnoreCaseOrderByImieAsc(nazwisko);
    }

    public List<Klient> findKlientsWithActiveRentals() {
        log.debug("Pobieranie klientów z aktywnymi wypożyczeniami");
        return klientRepository.findKlientsWithActiveRentals();
    }

    public List<Klient> findKlientsWithoutRentals() {
        log.debug("Pobieranie klientów bez wypożyczeń");
        return klientRepository.findKlientsWithoutRentals();
    }

    public boolean canRentCar(Long klientId) {
        log.debug("Sprawdzanie czy klient {} może wypożyczyć samochód", klientId);

        if (!klientRepository.existsById(klientId)) {
            return false;
        }

        return true;
    }

    public boolean hasActiveRentals(Long klientId) {
        return klientRepository.hasActiveRentals(klientId);
    }

    public long countRentals(Long klientId) {
        return klientRepository.countRentalsByKlientId(klientId);
    }

    public List<Klient> findMostActiveKlients() {
        log.debug("Pobieranie najaktywniejszych klientów");
        return klientRepository.findMostActiveKlients();
    }

    public boolean emailExists(String email) {
        return klientRepository.existsByEmail(email);
    }

    public List<Klient> findByEmailDomain(String domain) {
        log.debug("Pobieranie klientów z domeną: {}", domain);
        return klientRepository.findByEmailContainingIgnoreCaseOrderByEmailAsc("@" + domain);
    }

    // ===== METODY WALIDACJI + POMOCNICZE =====

    private void validateKlient(Klient klient) {
        // Walidacja imienia
        if (klient.getImie() == null || klient.getImie().trim().isEmpty()) {
            throw new IllegalArgumentException("Imię jest wymagane");
        }

        if (klient.getImie().trim().length() < 2) {
            throw new IllegalArgumentException("Imię musi mieć co najmniej 2 znaki");
        }

        if (klient.getImie().trim().length() > 50) {
            throw new IllegalArgumentException("Imię nie może mieć więcej niż 50 znaków");
        }

        if (klient.getNazwisko() == null || klient.getNazwisko().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwisko jest wymagane");
        }

        if (klient.getNazwisko().trim().length() < 2) {
            throw new IllegalArgumentException("Nazwisko musi mieć co najmniej 2 znaki");
        }

        if (klient.getNazwisko().trim().length() > 50) {
            throw new IllegalArgumentException("Nazwisko nie może mieć więcej niż 50 znaków");
        }

        if (klient.getEmail() == null || klient.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email jest wymagany");
        }

        if (!EMAIL_PATTERN.matcher(klient.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Email ma nieprawidłowy format");
        }

        if (klient.getEmail().trim().length() > 100) {
            throw new IllegalArgumentException("Email nie może mieć więcej niż 100 znaków");
        }

        if (containsInvalidCharacters(klient.getImie()) ||
                containsInvalidCharacters(klient.getNazwisko())) {
            throw new IllegalArgumentException("Imię i nazwisko mogą zawierać tylko litery, spacje, myślniki i apostrofy");
        }
    }

    private String normalizeText(String text) {
        if (text == null) return null;

        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return normalized;

        return normalized.substring(0, 1).toUpperCase() +
                normalized.substring(1).toLowerCase();
    }

    private boolean containsInvalidCharacters(String text) {
        if (text == null) return false;

        return !text.matches("[\\p{L}\\s\\-']+");
    }
}

