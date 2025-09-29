import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Wypozyczenie, WypozyczenieRequest } from '../models/rental.model';

@Injectable({
  providedIn: 'root'
})
export class WypozyczenieService {
  private apiUrl = `${environment.apiUrl}/wypozyczenia`;

  constructor(private http: HttpClient) { }

  getAllRentals(): Observable<Wypozyczenie[]> {
    return this.http.get<Wypozyczenie[]>(this.apiUrl);
  }

  rentCar(request: WypozyczenieRequest): Observable<Wypozyczenie> {
    return this.http.post<Wypozyczenie>(`${this.apiUrl}/wypozycz`, request);
  }

  returnCar(id: number): Observable<Wypozyczenie> {
    return this.http.put<Wypozyczenie>(`${this.apiUrl}/${id}/zwroc`, null);
  }

  getActiveRentals(): Observable<Wypozyczenie[]> {
    return this.http.get<Wypozyczenie[]>(`${this.apiUrl}/aktywne`);
  }
}
