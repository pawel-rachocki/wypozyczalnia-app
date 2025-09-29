import { Car } from './car.model';
import { Client } from './client.model';

export interface Rental {
  id?: number;
  client: Client;
  car: Car;
  rentalDate: string;
  returnDate?: string;
  totalCost: number;
  status: 'AKTYWNE' | 'ZAKONCZONE' | 'PRZETERMINOWANE' | 'ANULOWANE';
}

export interface RentalRequest {
  clientId: number;
  carId: number;
  rentalDate: string;
  plannedReturnDate: string;
}
