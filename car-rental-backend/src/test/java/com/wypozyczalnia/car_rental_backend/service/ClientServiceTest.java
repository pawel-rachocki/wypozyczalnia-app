package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.Client;
import com.wypozyczalnia.car_rental_backend.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFirstName("Jan");
        testClient.setLastName("Kowalski");
        testClient.setEmail("jan.kowalski@email.com");
    }

    @Test
    void shouldReturnAllClients() {
        // given
        List<Client> expectedKlienci = Arrays.asList(testClient);
        when(clientRepository.findAll()).thenReturn(expectedKlienci);

        // when
        List<Client> result = clientService.findAll();

        // then
        assertEquals(1, result.size());
        assertEquals("Jan", result.get(0).getFirstName());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnClientWhenValidId() {
        // given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        // when
        Client result = clientService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals("Jan", result.getFirstName());
        assertEquals("Kowalski", result.getLastName());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void shouldSaveValidClient() {
        // given
        Client newClient = new Client("Anna", "Nowak", "anna.nowak@email.com");
        when(clientRepository.existsByEmail("anna.nowak@email.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);

        // when
        Client result = clientService.save(newClient);

        // then
        assertNotNull(result);
        assertEquals("Anna", result.getFirstName());
        assertEquals("Nowak", result.getLastName());
        assertEquals("anna.nowak@email.com", result.getEmail());
        verify(clientRepository, times(1)).existsByEmail("anna.nowak@email.com");
        verify(clientRepository, times(1)).save(newClient);
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateEmail() {
        // given
        Client duplicateClient = new Client("Anna", "Nowak", "jan.kowalski@email.com");
        when(clientRepository.existsByEmail("jan.kowalski@email.com")).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.save(duplicateClient)
        );

        assertEquals("Client with email jan.kowalski@email.com already exists", exception.getMessage());
        verify(clientRepository, times(1)).existsByEmail("jan.kowalski@email.com");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInvalidEmail() {
        // given
        Client invalidClient = new Client("Jan", "Kowalski", "invalid-email");

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.save(invalidClient)
        );

        assertEquals("Email has invalid format", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldUpdateExistingClient() {
        // given
        Client updateData = new Client("Jan", "Nowak", "jan.nowak@email.com");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.existsByEmail("jan.nowak@email.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // when
        Client result = clientService.update(1L, updateData);

        // then
        assertNotNull(result);
        assertEquals("Jan", testClient.getFirstName());
        assertEquals("Nowak", testClient.getLastName());
        assertEquals("jan.nowak@email.com", testClient.getEmail());
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(testClient);
    }

    @Test
    void shouldDeleteClientWithoutActiveRentals() {
        // given
        when(clientRepository.hasActiveRentals(1L)).thenReturn(false);
        doNothing().when(clientRepository).deleteById(1L);

        // when
        clientService.delete(1L);

        // then
        verify(clientRepository, times(1)).hasActiveRentals(1L);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingClientWithActiveRentals() {
        // given
        when(clientRepository.hasActiveRentals(1L)).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> clientService.delete(1L)
        );

        assertEquals("Cannot delete client with active rentals", exception.getMessage());
        verify(clientRepository, times(1)).hasActiveRentals(1L);
        verify(clientRepository, never()).deleteById(1L);
    }
}