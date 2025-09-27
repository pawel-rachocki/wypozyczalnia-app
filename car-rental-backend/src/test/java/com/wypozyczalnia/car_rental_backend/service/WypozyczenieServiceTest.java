package com.wypozyczalnia.car_rental_backend.service;

import com.wypozyczalnia.car_rental_backend.entity.*;
import com.wypozyczalnia.car_rental_backend.repository.WypozyczenieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WypozyczenieServiceTest {

    @Mock
    private WypozyczenieRepository wypozyczenieRepository;

    @Mock
    private SamochodService samochodService;

    @Mock
    private KlientService klientService;

    @InjectMocks
    private WypozyczenieService wypozyczenieService;

    private Wypozyczenie testWypozyczenie;
    private Klient testKlient;
    private Samochod testSamochod;

    @BeforeEach
    void setUp() {
        testKlient = new Klient();
        testKlient.setId(1L);
        testKlient.setImie("Jan");
        testKlient.setNazwisko("Kowalski");
        testKlient.setEmail("jan.kowalski@email.com");

        testSamochod = new Samochod();
        testSamochod.setId(1L);
        testSamochod.setMarka("Toyota");
        testSamochod.setModel("Corolla");
        testSamochod.setCenaZaDzien(BigDecimal.valueOf(100.00));
        testSamochod.setStatus(StatusSamochodu.DOSTEPNY);

        testWypozyczenie = new Wypozyczenie();
        testWypozyczenie.setId(1L);
        testWypozyczenie.setKlient(testKlient);
        testWypozyczenie.setSamochod(testSamochod);
        testWypozyczenie.setDataWypozyczenia(LocalDate.now());
        testWypozyczenie.setDataZwrotu(LocalDate.now().plusDays(3));
        testWypozyczenie.setKosztCalkowity(BigDecimal.valueOf(300.00));
        testWypozyczenie.setStatus(StatusWypozyczenia.AKTYWNE);
    }

    @Test
    void shouldReturnAllWypozyczenia() {
        // given
        List<Wypozyczenie> expectedWypozyczenia = Arrays.asList(testWypozyczenie);
        when(wypozyczenieRepository.findAll()).thenReturn(expectedWypozyczenia);

        // when
        List<Wypozyczenie> result = wypozyczenieService.findAll();

        // then
        assertEquals(1, result.size());
        assertEquals(testKlient, result.get(0).getKlient());
        verify(wypozyczenieRepository, times(1)).findAll();
    }

    @Test
    void shouldSuccessfullyRentCar() {
        // given
        LocalDate dataWypozyczenia = LocalDate.now();
        LocalDate planowanaDataZwrotu = LocalDate.now().plusDays(3);

        when(klientService.findById(1L)).thenReturn(Optional.of(testKlient));
        when(samochodService.findById(1L)).thenReturn(Optional.of(testSamochod));
        when(klientService.canRentCar(1L)).thenReturn(true);
        when(samochodService.isAvailable(1L)).thenReturn(true);
        when(wypozyczenieRepository.existsBySamochodIdAndStatus(1L, StatusWypozyczenia.AKTYWNE)).thenReturn(false);
        when(wypozyczenieRepository.save(any(Wypozyczenie.class))).thenReturn(testWypozyczenie);
        doNothing().when(samochodService).markAsRented(1L);

        // when
        Wypozyczenie result = wypozyczenieService.rentCar(1L, 1L, dataWypozyczenia, planowanaDataZwrotu);

        // then
        assertNotNull(result);
        assertEquals(testKlient, result.getKlient());
        assertEquals(testSamochod, result.getSamochod());
        verify(wypozyczenieRepository, times(1)).save(any(Wypozyczenie.class));
        verify(samochodService, times(1)).markAsRented(1L);
    }

    @Test
    void shouldThrowExceptionWhenRentingUnavailableCar() {
        // given
        LocalDate dataWypozyczenia = LocalDate.now();
        LocalDate planowanaDataZwrotu = LocalDate.now().plusDays(3);

        when(klientService.findById(1L)).thenReturn(Optional.of(testKlient));
        when(samochodService.findById(1L)).thenReturn(Optional.of(testSamochod));
        when(klientService.canRentCar(1L)).thenReturn(true);
        when(samochodService.isAvailable(1L)).thenReturn(false);

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> wypozyczenieService.rentCar(1L, 1L, dataWypozyczenia, planowanaDataZwrotu)
        );

        assertEquals("Samochód nie jest dostępny do wypożyczenia", exception.getMessage());
        verify(wypozyczenieRepository, never()).save(any(Wypozyczenie.class));
        verify(samochodService, never()).markAsRented(1L);
    }

    @Test
    void shouldThrowExceptionWhenRentingWithInvalidDates() {
        // given
        LocalDate dataWypozyczenia = LocalDate.now();
        LocalDate planowanaDataZwrotu = LocalDate.now().minusDays(1); // Data w przeszłości

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wypozyczenieService.rentCar(1L, 1L, dataWypozyczenia, planowanaDataZwrotu)
        );

        assertEquals("Data zwrotu musi być później niż data wypożyczenia", exception.getMessage());
        verify(wypozyczenieRepository, never()).save(any(Wypozyczenie.class));
    }

    @Test
    void shouldSuccessfullyReturnCar() {
        // given
        LocalDate dataZwrotu = LocalDate.now();
        when(wypozyczenieRepository.findById(1L)).thenReturn(Optional.of(testWypozyczenie));
        when(wypozyczenieRepository.save(any(Wypozyczenie.class))).thenReturn(testWypozyczenie);
        doNothing().when(samochodService).markAsAvailable(1L);

        // when
        Wypozyczenie result = wypozyczenieService.returnCar(1L, dataZwrotu);

        // then
        assertNotNull(result);
        assertEquals(StatusWypozyczenia.ZAKONCZONE, testWypozyczenie.getStatus());
        assertEquals(dataZwrotu, testWypozyczenie.getDataZwrotu());
        verify(wypozyczenieRepository, times(1)).save(testWypozyczenie);
        verify(samochodService, times(1)).markAsAvailable(1L);
    }

    @Test
    void shouldReturnActiveRentals() {
        // given
        List<Wypozyczenie> activeRentals = Arrays.asList(testWypozyczenie);
        when(wypozyczenieRepository.findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia.AKTYWNE))
                .thenReturn(activeRentals);

        // when
        List<Wypozyczenie> result = wypozyczenieService.findActiveRentals();

        // then
        assertEquals(1, result.size());
        assertEquals(StatusWypozyczenia.AKTYWNE, result.get(0).getStatus());
        verify(wypozyczenieRepository, times(1))
                .findByStatusOrderByDataWypozyczeniaDesc(StatusWypozyczenia.AKTYWNE);
    }
    @Test
    void shouldCancelRentalWhenReturnBeforeStartDate() {
        // given
        LocalDate dataZwrotu = LocalDate.now().minusDays(1);
        when(wypozyczenieRepository.findById(1L)).thenReturn(Optional.of(testWypozyczenie));
        when(wypozyczenieRepository.save(any(Wypozyczenie.class))).thenReturn(testWypozyczenie);
        doNothing().when(samochodService).markAsAvailable(1L);

        // when
        Wypozyczenie result = wypozyczenieService.returnCar(1L, dataZwrotu);

        // then
        assertEquals(StatusWypozyczenia.ANULOWANE, testWypozyczenie.getStatus());
        verify(samochodService, times(1)).markAsAvailable(1L);
    }
}