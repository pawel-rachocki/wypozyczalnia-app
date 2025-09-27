package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.entity.Klient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class KlientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KlientRepository klientRepository;

    private Klient testKlient;

    @BeforeEach
    void setUp() {
        testKlient = new Klient();
        testKlient.setImie("Jan");
        testKlient.setNazwisko("Kowalski");
        testKlient.setEmail("jan.kowalski@email.com");

        entityManager.persistAndFlush(testKlient);
    }

    @Test
    void shouldFindKlientByEmail() {
        // given - dane w setUp()

        // when
        Optional<Klient> result = klientRepository.findByEmail("jan.kowalski@email.com");

        // then
        assertTrue(result.isPresent());
        assertEquals("Jan", result.get().getImie());
        assertEquals("Kowalski", result.get().getNazwisko());
        assertEquals("jan.kowalski@email.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotExists() {
        // given - dane w setUp()

        // when
        Optional<Klient> result = klientRepository.findByEmail("nieistniejacy@email.com");

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // given - dane w setUp()

        // when
        boolean exists = klientRepository.existsByEmail("jan.kowalski@email.com");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // given - dane w setUp()

        // when
        boolean exists = klientRepository.existsByEmail("nieistniejacy@email.com");

        // then
        assertFalse(exists);
    }
}