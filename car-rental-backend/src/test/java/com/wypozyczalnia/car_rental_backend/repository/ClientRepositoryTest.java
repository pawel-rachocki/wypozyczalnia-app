package com.wypozyczalnia.car_rental_backend.repository;

import com.wypozyczalnia.car_rental_backend.model.entity.Client;
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
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setFirstName("Jan");
        testClient.setLastName("Kowalski");
        testClient.setEmail("jan.kowalski@email.com");

        entityManager.persistAndFlush(testClient);
    }

    @Test
    void shouldFindClientByEmail() {
        // given - dane w setUp()

        // when
        Optional<Client> result = clientRepository.findByEmail("jan.kowalski@email.com");

        // then
        assertTrue(result.isPresent());
        assertEquals("Jan", result.get().getFirstName());
        assertEquals("Kowalski", result.get().getLastName());
        assertEquals("jan.kowalski@email.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotExists() {
        // given - dane w setUp()

        // when
        Optional<Client> result = clientRepository.findByEmail("nieistniejacy@email.com");

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // given - dane w setUp()

        // when
        boolean exists = clientRepository.existsByEmail("jan.kowalski@email.com");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // given - dane w setUp()

        // when
        boolean exists = clientRepository.existsByEmail("nieistniejacy@email.com");

        // then
        assertFalse(exists);
    }
}