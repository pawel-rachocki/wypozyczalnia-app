package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SamochodRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SamochodRepository samochodRepository;

    private Samochod dostepnySamochod;
    private Samochod wypozyczonySamochod;

    @BeforeEach
    void setUp() {
        dostepnySamochod = new Samochod();
        dostepnySamochod.setMarka("Toyota");
        dostepnySamochod.setModel("Corolla");
        dostepnySamochod.setCenaZaDzien(BigDecimal.valueOf(100.00));
        dostepnySamochod.setStatus(StatusSamochodu.DOSTEPNY);

        wypozyczonySamochod = new Samochod();
        wypozyczonySamochod.setMarka("Honda");
        wypozyczonySamochod.setModel("Civic");
        wypozyczonySamochod.setCenaZaDzien(BigDecimal.valueOf(120.00));
        wypozyczonySamochod.setStatus(StatusSamochodu.WYPOZYCZONY);

        entityManager.persistAndFlush(dostepnySamochod);
        entityManager.persistAndFlush(wypozyczonySamochod);
    }

    @Test
    void shouldFindAvailableCarsSortedByMarkaAndModel() {
        // given - dane w setUp()

        // when
        List<Samochod> result = samochodRepository.findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.DOSTEPNY);

        // then
        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getMarka());
        assertEquals("Corolla", result.get(0).getModel());
        assertEquals(StatusSamochodu.DOSTEPNY, result.get(0).getStatus());
    }

    @Test
    void shouldFindRentedCars() {
        // given - dane w setUp()

        // when
        List<Samochod> result = samochodRepository.findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.WYPOZYCZONY);

        // then
        assertEquals(1, result.size());
        assertEquals("Honda", result.get(0).getMarka());
        assertEquals(StatusSamochodu.WYPOZYCZONY, result.get(0).getStatus());
    }

    @Test
    void shouldReturnTrueWhenCarExists() {
        // given - dane w setUp()

        // when
        boolean exists = samochodRepository.existsByMarkaIgnoreCaseAndModelIgnoreCase("TOYOTA", "corolla");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenCarDoesNotExist() {
        // given - dane w setUp()

        // when
        boolean exists = samochodRepository.existsByMarkaIgnoreCaseAndModelIgnoreCase("BMW", "X5");

        // then
        assertFalse(exists);
    }

    @Test
    void shouldFindCarsByMarkaIgnoreCase() {
        // given
        Samochod anotherToyota = new Samochod();
        anotherToyota.setMarka("Toyota");
        anotherToyota.setModel("Camry");
        anotherToyota.setCenaZaDzien(BigDecimal.valueOf(150.00));
        anotherToyota.setStatus(StatusSamochodu.DOSTEPNY);
        entityManager.persistAndFlush(anotherToyota);

        // when
        List<Samochod> result = samochodRepository.findByMarkaIgnoreCaseOrderByModelAsc("toyota");

        // then
        assertEquals(2, result.size());
        assertEquals("Camry", result.get(0).getModel());
        assertEquals("Corolla", result.get(1).getModel());
        result.forEach(samochod -> assertEquals("Toyota", samochod.getMarka()));
    }
}