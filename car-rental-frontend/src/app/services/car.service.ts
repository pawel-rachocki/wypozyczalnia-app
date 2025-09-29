import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Samochod } from '../models/car.model';

@Injectable({
  providedIn: 'root'
})
export class SamochodService {
  private apiUrl = `${environment.apiUrl}/samochody`;

  constructor(private http: HttpClient) { }

  getAllSamochody(): Observable<Samochod[]> {
    return this.http.get<Samochod[]>(this.apiUrl);
  }

  getSamochodById(id: number): Observable<Samochod> {
    return this.http.get<Samochod>(`${this.apiUrl}/${id}`);
  }

  createSamochod(samochod: Samochod): Observable<Samochod> {
    return this.http.post<Samochod>(this.apiUrl, samochod);
  }

  updateSamochod(id: number, samochod: Samochod): Observable<Samochod> {
    return this.http.put<Samochod>(`${this.apiUrl}/${id}`, samochod);
  }

  deleteSamochod(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAvailableSamochody(): Observable<Samochod[]> {
    return this.http.get<Samochod[]>(`${this.apiUrl}/dostepne`);
  }
}
