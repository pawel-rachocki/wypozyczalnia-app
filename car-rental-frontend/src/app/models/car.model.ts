export interface Samochod {
  id?: number;
  brand: string;
  model: string;
  dailyPrice: number;
  status: 'DOSTEPNY' | 'WYPOZYCZONY';
}
