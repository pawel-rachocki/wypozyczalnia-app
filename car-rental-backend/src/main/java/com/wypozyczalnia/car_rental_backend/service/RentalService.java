package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.*;
import com.wypozyczalnia.car_rental_backend.model.exception.RentalNotFoundException;
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

        Klient klient = klientService.findById(klientId);

        Samochod samochod = samochodService.findById(samochodId);

        if (wypozyczenieRepository.existsBySamochodIdAndStatus(samochodId, StatusWypozyczenia.AKTYWNE)) {
            throw new IllegalStateException("Samochód jest już wypożyczony");
        }

        int liczbaDni = Period.between(dataWypozyczenia, planowanaDataZwrotu).getDays();
        if (liczbaDni == 0) { // W przypadku wypożyczenia i zwrotu tego samego dnia, naliczamy 1 dzień
            liczbaDni = 1;
        }

        BigDecimal kosztCalkowity = samochod.getCenaZaDzien()
                .multiply(BigDecimal.valueOf(liczbaDni));

        Wypozyczenie wypozyczenie = new Wypozyczenie(
                klient, samochod, dataWypozyczenia, kosztCalkowity, planowanaDataZwrotu
        );

        Wypozyczenie savedRental = wypozyczenieRepository.save(wypozyczenie);

        samochodService.markAsRented(samochodId);

        return savedRental;
    }

    public List<Wypozyczenie> findByStatus(StatusWypozyczenia status) {
        return wypozyczenieRepository.findByStatusOrderByDataWypozyczeniaDesc(status);
    }

    public List<Wypozyczenie> findActiveRentals() {
        return findByStatus(StatusWypozyczenia.AKTYWNE);
    }

    @Transactional
    public Wypozyczenie returnCar(Long wypozyczenieId) {
        LocalDate dataZwrotu = LocalDate.now();

        Wypozyczenie wypozyczenie = wypozyczenieRepository.findById(wypozyczenieId)
                .orElseThrow(() -> new RentalNotFoundException(wypozyczenieId));

        if (!StatusWypozyczenia.AKTYWNE.equals(wypozyczenie.getStatus())) {
            throw new IllegalStateException("Wypożyczenie nie jest aktywne");
        }

        if (dataZwrotu.isBefore(wypozyczenie.getDataWypozyczenia())) {
            wypozyczenie.setStatus(StatusWypozyczenia.ANULOWANE);
            wypozyczenie.setDataZwrotu(dataZwrotu);
            wypozyczenie.setKosztCalkowity(BigDecimal.ZERO);
        } else {
            wypozyczenie.setDataZwrotu(dataZwrotu);
            wypozyczenie.setStatus(StatusWypozyczenia.ZAKONCZONE);
        }

        Wypozyczenie updated = wypozyczenieRepository.save(wypozyczenie);
        samochodService.markAsAvailable(wypozyczenie.getSamochod().getId());

        return updated;
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

    }
}
