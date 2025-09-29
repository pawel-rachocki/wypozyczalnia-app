export interface Car {
  id?: number;
  brand: string;
  model: string;
  dailyPrice: number;
  status: 'DOSTEPNY' | 'WYPOZYCZONY';
}
