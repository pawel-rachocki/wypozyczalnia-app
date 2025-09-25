package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusWypozyczenia;
import com.wypozyczalnia.car_rental_backend.entity.Wypozyczenie;
import com.wypozyczalnia.car_rental_backend.repository.WypozyczenieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WypozyczenieService {

    private final WypozyczenieRepository wypozyczenieRepository;
    private final SamochodService samochodService;
    private final KlientService klientService;

    // ===== OPERACJE CRUD =====

    public List<Wypozyczenie> findAll() {
        return wypozyczenieRepository.findAll();
    }

    public Optional<Wypozyczenie> findById(Long id) {
        return wypozyczenieRepository.findById(id);
    }

    @Transactional
    public Wypozyczenie rentCar(Long klientId, Long samochodId, LocalDate dataWypozyczenia, LocalDate planowanaDataZwrotu) {
        validateRentalData(klientId, samochodId, dataWypozyczenia, planowanaDataZwrotu);

        Klient klient = klientService.findById(klientId)
                .orElseThrow(() -> new IllegalArgumentException("Klient o ID " + klientId + " nie istnieje"));

        Samochod samochod = samochodService.findById(samochodId)
                .orElseThrow(() -> new IllegalArgumentException("Samochód o ID " + samochodId + " nie istnieje"));

        if (!klientService.canRentCar(klientId)) {
            throw new IllegalStateException("Klient nie może wypożyczyć samochodu (możliwe przeterminowane wypożyczenia)");
        }

        if (!samochodService.isAvailable(samochodId)) {
            throw new IllegalStateException("Samochód nie jest dostępny do wypożyczenia");
        }

        if (wypozyczenieRepository.existsBySamochodIdAndStatus(samochodId, StatusWypozyczenia.AKTYWNE)) {
            throw new IllegalStateException("Samochód jest już wypożyczony");
        }

        int liczbaDni = Period.between(dataWypozyczenia, planowanaDataZwrotu).getDays();
        if (liczbaDni <= 0) {
            liczbaDni = 1;
        }

        BigDecimal kosztCalkowity = samochod.getCenaZaDzien()
                .multiply(BigDecimal.valueOf(liczbaDni));

        Wypozyczenie wypozyczenie = new Wypozyczenie(
                klient, samochod, dataWypozyczenia, kosztCalkowity
        );
        wypozyczenie.setDataZwrotu(planowanaDataZwrotu);
        wypozyczenie.setStatus(StatusWypozyczenia.AKTYWNE);

        Wypozyczenie saved = wypozyczenieRepository.save(wypozyczenie);

        samochodService.markAsRented(samochodId);

        return saved;
    }

    @Transactional
    public Wypozyczenie returnCar(Long wypozyczenieId, LocalDate dataZwrotu) {

        if (dataZwrotu == null) {
            throw new IllegalArgumentException("Data zwrotu jest wymagana");
        }

        Wypozyczenie wypozyczenie = wypozyczenieRepository.findById(wypozyczenieId)
                .orElseThrow(() -> new IllegalArgumentException("Wypożyczenie o ID " + wypozyczenieId + " nie istnieje"));

        if (!StatusWypozyczenia.AKTYWNE.equals(wypozyczenie.getStatus())) {
            throw new IllegalStateException("Wypożyczenie nie jest aktywne");
        }

        if (dataZwrotu.isBefore(wypozyczenie.getDataWypozyczenia())) {
            throw new IllegalArgumentException("Data zwrotu nie może być wcześniejsza niż data wypożyczenia");
        }

        wypozyczenie.setDataZwrotu(dataZwrotu);
        wypozyczenie.setStatus(StatusWypozyczenia.ZAKONCZONE);

        LocalDate planowanaDataZwrotu = wypozyczenie.getDataZwrotu();
        if (planowanaDataZwrotu != null && dataZwrotu.isAfter(planowanaDataZwrotu)) {
            int dniPrzeterminowania = Period.between(planowanaDataZwrotu, dataZwrotu).getDays();
        }

        Wypozyczenie updated = wypozyczenieRepository.save(wypozyczenie);

        samochodService.markAsAvailable(wypozyczenie.getSamochod().getId());

        return updated;
    }

    @Transactional
    public Wypozyczenie cancelRental(Long wypozyczenieId, String powod) {
        Wypozyczenie wypozyczenie = wypozyczenieRepository.findById(wypozyczenieId)
                .orElseThrow(() -> new IllegalArgumentException("Wypożyczenie o ID " + wypozyczenieId + " nie istnieje"));

        if (!StatusWypozyczenia.AKTYWNE.equals(wypozyczenie.getStatus())) {
            throw new IllegalStateException("Można anulować tylko aktywne wypożyczenia");
        }

        if (LocalDate.now().isAfter(wypozyczenie.getDataWypozyczenia())) {
            throw new IllegalStateException("Nie można anulować rozpoczętego wypożyczenia");
        }

        wypozyczenie.setStatus(StatusWypozyczenia.ANULOWANE);
        Wypozyczenie updated = wypozyczenieRepository.save(wypozyczenie);

        samochodService.markAsAvailable(wypozyczenie.getSamochod().getId());

        return updated;
    }

    // ===== OPERACJE BIZNESOWE =====

    public List<Wypozyczenie> findByKlient(Long klientId) {
        return wypozyczenieRepository.findByKlientIdOrderByDataWypozyczeniaDesc(klientId);
    }

    public List<Wypozyczenie> findBySamochod(Long samochodId) {
        return wypozyczenieRepository.findBySamochodIdOrderByDataWypozyczeniaDesc(samochodId);
    }

    public List<Wypozyczenie> findByStatus(StatusWypozyczenia status) {
        return wypozyczenieRepository.findByStatusOrderByDataWypozyczeniaDesc(status);
    }

    public List<Wypozyczenie> findActiveRentals() {
        return findByStatus(StatusWypozyczenia.AKTYWNE);
    }

    public List<Wypozyczenie> findOverdueRentals() {
        LocalDate limitDate = LocalDate.now().minusDays(1);
        return wypozyczenieRepository.findOverdueRentals(limitDate);
    }

    public List<Wypozyczenie> findRentalsEndingToday() {
        return wypozyczenieRepository.findRentalsEndingToday(LocalDate.now());
    }

    @Transactional
    public void markOverdueRentals() {
        List<Wypozyczenie> overdue = findOverdueRentals();
        for (Wypozyczenie wypozyczenie : overdue) {
            wypozyczenie.setStatus(StatusWypozyczenia.PRZETERMINOWANE);
            wypozyczenieRepository.save(wypozyczenie);
        }
    }

    public BigDecimal calculateTotalRevenue() {
        BigDecimal revenue = wypozyczenieRepository.calculateTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal calculateRevenueInPeriod(LocalDate startDate, LocalDate endDate) {
        BigDecimal revenue = wypozyczenieRepository.calculateRevenueInPeriod(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public long countByStatus(StatusWypozyczenia status) {
        return wypozyczenieRepository.countByStatus(status);
    }

    // ===== METODY WALIDACJI DANYCH =====

    private void validateRentalData(Long klientId, Long samochodId, LocalDate dataWypozyczenia, LocalDate planowanaDataZwrotu) {
        if (klientId == null) {
            throw new IllegalArgumentException("ID klienta jest wymagane");
        }

        if (samochodId == null) {
            throw new IllegalArgumentException("ID samochodu jest wymagane");
        }

        if (dataWypozyczenia == null) {
            throw new IllegalArgumentException("Data wypożyczenia jest wymagana");
        }

        if (planowanaDataZwrotu == null) {
            throw new IllegalArgumentException("Planowana data zwrotu jest wymagana");
        }

        if (dataWypozyczenia.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data wypożyczenia nie może być w przeszłości");
        }

        if (planowanaDataZwrotu.isBefore(dataWypozyczenia) || planowanaDataZwrotu.isEqual(dataWypozyczenia)) {
            throw new IllegalArgumentException("Data zwrotu musi być później niż data wypożyczenia");
        }

        if (Period.between(dataWypozyczenia, planowanaDataZwrotu).getDays() > 365) {
            throw new IllegalArgumentException("Maksymalny okres wypożyczenia to 365 dni");
        }
    }
}
