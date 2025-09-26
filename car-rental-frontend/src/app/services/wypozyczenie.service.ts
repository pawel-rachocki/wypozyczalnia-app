import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Wypozyczenie, WypozyczenieRequest } from '../models/wypozyczenie.model';

@Injectable({
  providedIn: 'root'
})
export class WypozyczenieService {
  private apiUrl = `${environment.apiUrl}/wypozyczenia`;

  constructor(private http: HttpClient) { }

  getAllWypozyczenia(): Observable<Wypozyczenie[]> {
    return this.http.get<Wypozyczenie[]>(this.apiUrl);
  }

  getWypozyczenieById(id: number): Observable<Wypozyczenie> {
    return this.http.get<Wypozyczenie>(`${this.apiUrl}/${id}`);
  }

  rentCar(request: WypozyczenieRequest): Observable<Wypozyczenie> {
    return this.http.post<Wypozyczenie>(`${this.apiUrl}/wypozycz`, request);
  }

  returnCar(id: number, dataZwrotu: string): Observable<Wypozyczenie> {
    const params = new HttpParams().set('dataZwrotu', dataZwrotu);
    return this.http.put<Wypozyczenie>(`${this.apiUrl}/${id}/zwroc`, null, { params });
  }

  getActiveRentals(): Observable<Wypozyczenie[]> {
    return this.http.get<Wypozyczenie[]>(`${this.apiUrl}/aktywne`);
  }

  getRentalsByKlient(klientId: number): Observable<Wypozyczenie[]> {
    return this.http.get<Wypozyczenie[]>(`${this.apiUrl}/klient/${klientId}`);
  }
}
