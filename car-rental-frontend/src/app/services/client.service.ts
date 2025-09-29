import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Klient } from '../models/client.model';

@Injectable({
  providedIn: 'root'
})
export class KlientService {
  private apiUrl = `${environment.apiUrl}/klienci`;

  constructor(private http: HttpClient) { }

  getAllKlienci(): Observable<Klient[]> {
    return this.http.get<Klient[]>(this.apiUrl);
  }

  getKlientById(id: number): Observable<Klient> {
    return this.http.get<Klient>(`${this.apiUrl}/${id}`);
  }

  createKlient(klient: Klient): Observable<Klient> {
    return this.http.post<Klient>(this.apiUrl, klient);
  }

  updateKlient(id: number, klient: Klient): Observable<Klient> {
    return this.http.put<Klient>(`${this.apiUrl}/${id}`, klient);
  }

  deleteKlient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
