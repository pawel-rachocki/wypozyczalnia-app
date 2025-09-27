package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.entity.Samochod;
import com.wypozyczalnia.car_rental_backend.entity.StatusSamochodu;
import com.wypozyczalnia.car_rental_backend.repository.SamochodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SamochodServiceTest {

    @Mock
    private SamochodRepository samochodRepository;

    @InjectMocks
    private SamochodService samochodService;

    private Samochod testSamochod;
    private Samochod testSamochod2;

    @BeforeEach
    void setUp() {
        testSamochod = new Samochod();
        testSamochod.setId(1L);
        testSamochod.setMarka("Toyota");
        testSamochod.setModel("Corolla");
        testSamochod.setCenaZaDzien(BigDecimal.valueOf(100.00));
        testSamochod.setStatus(StatusSamochodu.DOSTEPNY);

        testSamochod2 = new Samochod();
        testSamochod2.setId(2L);
        testSamochod2.setMarka("Honda");
        testSamochod2.setModel("Civic");
        testSamochod2.setCenaZaDzien(BigDecimal.valueOf(120.00));
        testSamochod2.setStatus(StatusSamochodu.WYPOZYCZONY);
    }

    // ===== TESTY FIND ALL =====

    @Test
    void shouldReturnAllSamochody() {
        // given
        List<Samochod> expectedSamochody = Arrays.asList(testSamochod, testSamochod2);
        when(samochodRepository.findAll()).thenReturn(expectedSamochody);

        // when
        List<Samochod> result = samochodService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getMarka());
        assertEquals("Honda", result.get(1).getMarka());
        verify(samochodRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoSamochody() {
        // given
        when(samochodRepository.findAll()).thenReturn(Arrays.asList());

        // when
        List<Samochod> result = samochodService.findAll();

        // then
        assertTrue(result.isEmpty());
        verify(samochodRepository, times(1)).findAll();
    }

    // ===== TESTY FIND BY ID =====

    @Test
    void shouldReturnSamochodWhenValidId() {
        // given
        when(samochodRepository.findById(1L)).thenReturn(Optional.of(testSamochod));

        // when
        Optional<Samochod> result = samochodService.findById(1L);

        // then
        assertTrue(result.isPresent());
        assertEquals("Toyota", result.get().getMarka());
        assertEquals("Corolla", result.get().getModel());
        verify(samochodRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenInvalidId() {
        // given
        when(samochodRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Samochod> result = samochodService.findById(999L);

        // then
        assertFalse(result.isPresent());
        verify(samochodRepository, times(1)).findById(999L);
    }

    // ===== TESTY SAVE =====

    @Test
    void shouldThrowExceptionWhenSavingWithNullMarka() {
        // given
        Samochod invalidSamochod = new Samochod(null, "Model", BigDecimal.valueOf(100.00), StatusSamochodu.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.save(invalidSamochod)
        );

        assertEquals("Marka samochodu jest wymagana", exception.getMessage());
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithEmptyModel() {
        // given
        Samochod invalidSamochod = new Samochod("Toyota", "", BigDecimal.valueOf(100.00), StatusSamochodu.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.save(invalidSamochod)
        );

        assertEquals("Model samochodu jest wymagany", exception.getMessage());
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInvalidPrice() {
        // given
        Samochod invalidSamochod = new Samochod("Toyota", "Corolla", BigDecimal.valueOf(-50.00), StatusSamochodu.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.save(invalidSamochod)
        );

        assertEquals("Cena za dzień musi być większa od zera", exception.getMessage());
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithTooHighPrice() {
        // given
        Samochod invalidSamochod = new Samochod("Toyota", "Corolla", BigDecimal.valueOf(15000.00), StatusSamochodu.DOSTEPNY);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.save(invalidSamochod)
        );

        assertEquals("Cena za dzień nie może przekraczać 10 000 zł", exception.getMessage());
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    // ===== TESTY UPDATE =====
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentSamochod() {
        // given
        Samochod updateData = new Samochod("Toyota", "Camry", BigDecimal.valueOf(150.00), StatusSamochodu.DOSTEPNY);
        when(samochodRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.update(999L, updateData)
        );

        assertEquals("Samochód o ID 999 nie istnieje", exception.getMessage());
        verify(samochodRepository, times(1)).findById(999L);
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    // ===== TESTY DELETE =====

    @Test
    void shouldDeleteAvailableSamochod() {
        // given
        when(samochodRepository.findById(1L)).thenReturn(Optional.of(testSamochod));
        doNothing().when(samochodRepository).deleteById(1L);

        // when
        samochodService.delete(1L);

        // then
        verify(samochodRepository, times(1)).findById(1L);
        verify(samochodRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingRentedSamochod() {
        // given
        when(samochodRepository.findById(2L)).thenReturn(Optional.of(testSamochod2));

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> samochodService.delete(2L)
        );

        assertEquals("Nie można usunąć wypożyczonego samochodu", exception.getMessage());
        verify(samochodRepository, times(1)).findById(2L);
        verify(samochodRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentSamochod() {
        // given
        when(samochodRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> samochodService.delete(999L)
        );

        assertEquals("Samochód o ID 999 nie istnieje", exception.getMessage());
        verify(samochodRepository, times(1)).findById(999L);
        verify(samochodRepository, never()).deleteById(anyLong());
    }

    // ===== TESTY FIND AVAILABLE =====

    @Test
    void shouldReturnAvailableSamochody() {
        // given
        List<Samochod> availableCars = Arrays.asList(testSamochod);
        when(samochodRepository.findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.DOSTEPNY)).thenReturn(availableCars);

        // when
        List<Samochod> result = samochodService.findAvailable();

        // then
        assertEquals(1, result.size());
        assertEquals(StatusSamochodu.DOSTEPNY, result.get(0).getStatus());
        verify(samochodRepository, times(1)).findByStatusOrderByMarkaAscModelAsc(StatusSamochodu.DOSTEPNY);
    }

    // ===== TESTY MARK AS RENTED/AVAILABLE =====

    @Test
    void shouldMarkSamochodAsRented() {
        // given
        when(samochodRepository.findById(1L)).thenReturn(Optional.of(testSamochod));
        when(samochodRepository.save(any(Samochod.class))).thenReturn(testSamochod);

        // when
        samochodService.markAsRented(1L);

        // then
        assertEquals(StatusSamochodu.WYPOZYCZONY, testSamochod.getStatus());
        verify(samochodRepository, times(1)).findById(1L);
        verify(samochodRepository, times(1)).save(testSamochod);
    }

    @Test
    void shouldThrowExceptionWhenMarkingAlreadyRentedSamochod() {
        // given
        when(samochodRepository.findById(2L)).thenReturn(Optional.of(testSamochod2));

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> samochodService.markAsRented(2L)
        );

        assertEquals("Samochód nie jest dostępny do wypożyczenia", exception.getMessage());
        verify(samochodRepository, times(1)).findById(2L);
        verify(samochodRepository, never()).save(any(Samochod.class));
    }

    @Test
    void shouldMarkSamochodAsAvailable() {
        // given
        when(samochodRepository.findById(2L)).thenReturn(Optional.of(testSamochod2));
        when(samochodRepository.save(any(Samochod.class))).thenReturn(testSamochod2);

        // when
        samochodService.markAsAvailable(2L);

        // then
        assertEquals(StatusSamochodu.DOSTEPNY, testSamochod2.getStatus());
        verify(samochodRepository, times(1)).findById(2L);
        verify(samochodRepository, times(1)).save(testSamochod2);
    }

    // ===== TESTY IS AVAILABLE =====

    @Test
    void shouldReturnTrueWhenSamochodIsAvailable() {
        // given
        when(samochodRepository.findById(1L)).thenReturn(Optional.of(testSamochod));

        // when
        boolean result = samochodService.isAvailable(1L);

        // then
        assertTrue(result);
        verify(samochodRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnFalseWhenSamochodIsRented() {
        // given
        when(samochodRepository.findById(2L)).thenReturn(Optional.of(testSamochod2));

        // when
        boolean result = samochodService.isAvailable(2L);

        // then
        assertFalse(result);
        verify(samochodRepository, times(1)).findById(2L);
    }

    @Test
    void shouldReturnFalseWhenSamochodNotExists() {
        // given
        when(samochodRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        boolean result = samochodService.isAvailable(999L);

        // then
        assertFalse(result);
        verify(samochodRepository, times(1)).findById(999L);
    }
}