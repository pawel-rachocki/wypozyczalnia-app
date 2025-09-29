package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.model.entity.Klient;
import com.wypozyczalnia.car_rental_backend.repository.KlientRepository;
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
class KlientServiceTest {

    @Mock
    private KlientRepository klientRepository;

    @InjectMocks
    private KlientService klientService;

    private Klient testKlient;

    @BeforeEach
    void setUp() {
        testKlient = new Klient();
        testKlient.setId(1L);
        testKlient.setImie("Jan");
        testKlient.setNazwisko("Kowalski");
        testKlient.setEmail("jan.kowalski@email.com");
    }

    @Test
    void shouldReturnAllKlienci() {
        // given
        List<Klient> expectedKlienci = Arrays.asList(testKlient);
        when(klientRepository.findAll()).thenReturn(expectedKlienci);

        // when
        List<Klient> result = klientService.findAll();

        // then
        assertEquals(1, result.size());
        assertEquals("Jan", result.get(0).getImie());
        verify(klientRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnKlientWhenValidId() {
        // given
        when(klientRepository.findById(1L)).thenReturn(Optional.of(testKlient));

        // when
        Klient result = klientService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals("Jan", result.getImie());
        assertEquals("Kowalski", result.getNazwisko());
        verify(klientRepository, times(1)).findById(1L);
    }

    @Test
    void shouldSaveValidKlient() {
        // given
        Klient newKlient = new Klient("Anna", "Nowak", "anna.nowak@email.com");
        when(klientRepository.existsByEmail("anna.nowak@email.com")).thenReturn(false);
        when(klientRepository.save(any(Klient.class))).thenReturn(newKlient);

        // when
        Klient result = klientService.save(newKlient);

        // then
        assertNotNull(result);
        assertEquals("Anna", result.getImie());
        assertEquals("Nowak", result.getNazwisko());
        assertEquals("anna.nowak@email.com", result.getEmail());
        verify(klientRepository, times(1)).existsByEmail("anna.nowak@email.com");
        verify(klientRepository, times(1)).save(newKlient);
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateEmail() {
        // given
        Klient duplicateKlient = new Klient("Anna", "Nowak", "jan.kowalski@email.com");
        when(klientRepository.existsByEmail("jan.kowalski@email.com")).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> klientService.save(duplicateKlient)
        );

        assertEquals("Klient z emailem jan.kowalski@email.com już istnieje w systemie", exception.getMessage());
        verify(klientRepository, times(1)).existsByEmail("jan.kowalski@email.com");
        verify(klientRepository, never()).save(any(Klient.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInvalidEmail() {
        // given
        Klient invalidKlient = new Klient("Jan", "Kowalski", "invalid-email");

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> klientService.save(invalidKlient)
        );

        assertEquals("Email ma nieprawidłowy format", exception.getMessage());
        verify(klientRepository, never()).save(any(Klient.class));
    }

    @Test
    void shouldUpdateExistingKlient() {
        // given
        Klient updateData = new Klient("Jan", "Nowak", "jan.nowak@email.com");
        when(klientRepository.findById(1L)).thenReturn(Optional.of(testKlient));
        when(klientRepository.existsByEmail("jan.nowak@email.com")).thenReturn(false);
        when(klientRepository.save(any(Klient.class))).thenReturn(testKlient);

        // when
        Klient result = klientService.update(1L, updateData);

        // then
        assertNotNull(result);
        assertEquals("Jan", testKlient.getImie());
        assertEquals("Nowak", testKlient.getNazwisko());
        assertEquals("jan.nowak@email.com", testKlient.getEmail());
        verify(klientRepository, times(1)).findById(1L);
        verify(klientRepository, times(1)).save(testKlient);
    }

    @Test
    void shouldDeleteKlientWithoutActiveRentals() {
        // given
        when(klientRepository.findById(1L)).thenReturn(Optional.of(testKlient));
        when(klientRepository.hasActiveRentals(1L)).thenReturn(false);
        doNothing().when(klientRepository).deleteById(1L);

        // when
        klientService.delete(1L);

        // then
        verify(klientRepository, times(1)).findById(1L);
        verify(klientRepository, times(1)).hasActiveRentals(1L);
        verify(klientRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingKlientWithActiveRentals() {
        // given
        when(klientRepository.findById(1L)).thenReturn(Optional.of(testKlient));
        when(klientRepository.hasActiveRentals(1L)).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> klientService.delete(1L)
        );

        assertEquals("Nie można usunąć klienta z aktywnymi wypożyczeniami", exception.getMessage());
        verify(klientRepository, times(1)).findById(1L);
        verify(klientRepository, times(1)).hasActiveRentals(1L);
        verify(klientRepository, never()).deleteById(1L);
    }
}