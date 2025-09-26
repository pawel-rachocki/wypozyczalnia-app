export interface Samochod {
  id?: number;
  marka: string;
  model: string;
  cenaZaDzien: number;
  status: 'DOSTEPNY' | 'WYPOZYCZONY';
}
