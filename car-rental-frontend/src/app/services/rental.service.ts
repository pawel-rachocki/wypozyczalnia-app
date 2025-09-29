import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rental, RentalRequest } from '../models/rental.model';

@Injectable({
  providedIn: 'root'
})
export class RentalService {
  private apiUrl = `${environment.apiUrl}/wypozyczenia`;

  constructor(private http: HttpClient) { }

  getAllRentals(): Observable<Rental[]> {
    return this.http.get<Rental[]>(this.apiUrl);
  }

  rentCar(request: RentalRequest): Observable<Rental> {
    return this.http.post<Rental>(`${this.apiUrl}/wypozycz`, request);
  }

  returnCar(id: number): Observable<Rental> {
    return this.http.put<Rental>(`${this.apiUrl}/${id}/zwroc`, null);
  }

  getActiveRentals(): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/aktywne`);
  }
}
