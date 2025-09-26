import { Samochod } from './samochod.model';
import { Klient } from './klient.model';

export interface Wypozyczenie {
  id?: number;
  klient: Klient;
  samochod: Samochod;
  dataWypozyczenia: string;
  dataZwrotu?: string;
  kosztCalkowity: number;
  status: 'AKTYWNE' | 'ZAKONCZONE' | 'PRZETERMINOWANE' | 'ANULOWANE';
}

export interface WypozyczenieRequest {
  klientId: number;
  samochodId: number;
  dataWypozyczenia: string;
  planowanaDataZwrotu: string;
}
