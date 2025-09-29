import { Samochod } from './car.model';
import { Klient } from './client.model';

export interface Wypozyczenie {
  id?: number;
  client: Klient;
  car: Samochod;
  rentalDate: string;
  returnDate?: string;
  totalCost: number;
  status: 'AKTYWNE' | 'ZAKONCZONE' | 'PRZETERMINOWANE' | 'ANULOWANE';
}

export interface WypozyczenieRequest {
  clientId: number;
  carId: number;
  rentalDate: string;
  plannedReturnDate: string;
}
