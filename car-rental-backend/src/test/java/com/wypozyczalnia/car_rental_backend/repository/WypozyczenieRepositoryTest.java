package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WypozyczenieRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WypozyczenieRepository wypozyczenieRepository;

    private Klient testKlient;
    private Samochod testSamochod;
    private Wypozyczenie aktywneWypozyczenie;
    private Wypozyczenie zakonczoneWypozyczenie;

    @BeforeEach
    void setUp() {
        // Klient
        testKlient = new Klient();
        testKlient.setImie("Jan");
        testKlient.setNazwisko("Kowalski");
        testKlient.setEmail("jan.kowalski@email.com");
        entityManager.persistAndFlush(testKlient);

        // Samochód
        testSamochod = new Samochod();
        testSamochod.setMarka("Toyota");
        testSamochod.setModel("Corolla");
        testSamochod.setCenaZaDzien(BigDecimal.valueOf(100.00));
        testSamochod.setStatus(StatusSamochodu.WYPOZYCZONY);
        entityManager.persistAndFlush(testSamochod);

        // Aktywne wypożyczenie
        aktywneWypozyczenie = new Wypozyczenie();
        aktywneWypozyczenie.setKlient(testKlient);
        aktywneWypozyczenie.setSamochod(testSamochod);
        aktywneWypozyczenie.setDataWypozyczenia(LocalDate.now().minusDays(2));
        aktywneWypozyczenie.setKosztCalkowity(BigDecimal.valueOf(300.00));
        aktywneWypozyczenie.setStatus(StatusWypozyczenia.AKTYWNE);
        entityManager.persistAndFlush(aktywneWypozyczenie);

        // Zakończone wypożyczenie
        zakonczoneWypozyczenie = new Wypozyczenie();
        zakonczoneWypozyczenie.setKlient(testKlient);
        zakonczoneWypozyczenie.setSamochod(testSamochod);
        zakonczoneWypozyczenie.setDataWypozyczenia(LocalDate.now().minusDays(10));
        zakonczoneWypozyczenie.setDataZwrotu(LocalDate.now().minusDays(7));
        zakonczoneWypozyczenie.setKosztCalkowity(BigDecimal.valueOf(300.00));
        zakonczoneWypozyczenie.setStatus(StatusWypozyczenia.ZAKONCZONE);
        entityManager.persistAndFlush(zakonczoneWypozyczenie);
    }

    @Test
    void shouldFindActiveRentalsSortedByDate() {
        // given - dane w setUp()

        // when
        List<Wypozyczenie> result = wypozyczenieRepository.findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia.AKTYWNE);

        // then
        assertEquals(1, result.size());
        assertEquals(StatusWypozyczenia.AKTYWNE, result.get(0).getStatus());
        assertEquals(testKlient, result.get(0).getKlient());
        assertEquals(testSamochod, result.get(0).getSamochod());
    }

    @Test
    void shouldFindCompletedRentals() {
        // given - dane w setUp()

        // when
        List<Wypozyczenie> result = wypozyczenieRepository.findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia.ZAKONCZONE);

        // then
        assertEquals(1, result.size());
        assertEquals(StatusWypozyczenia.ZAKONCZONE, result.get(0).getStatus());
        assertNotNull(result.get(0).getDataZwrotu());
    }

    @Test
    void shouldFindRentalsByKlientId() {
        // given - dane w setUp()

        // when
        List<Wypozyczenie> result = wypozyczenieRepository.findByKlientIdOrderByDataWypozyczeniaDesc(testKlient.getId());

        // then
        assertEquals(2, result.size()); // Aktywne + zakończone
        result.forEach(wypozyczenie -> assertEquals(testKlient.getId(), wypozyczenie.getKlient().getId()));

        // Sprawdź sortowanie (najnowsze pierwsze)
        assertTrue(result.get(0).getDataWypozyczenia().isAfter(result.get(1).getDataWypozyczenia()) ||
                result.get(0).getDataWypozyczenia().isEqual(result.get(1).getDataWypozyczenia()));
    }

    @Test
    void shouldReturnTrueWhenActiveRentalExistsForCar() {
        // given - dane w setUp()

        // when
        boolean exists = wypozyczenieRepository.existsBySamochodIdAndStatus(testSamochod.getId(), StatusWypozyczenia.AKTYWNE);

        // then
        assertTrue(exists);
    }

}