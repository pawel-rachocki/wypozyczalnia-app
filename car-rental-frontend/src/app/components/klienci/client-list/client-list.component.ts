import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { KlientService } from '../../../services/client.service';
import { Klient } from '../../../models/client.model';

@Component({
  selector: 'app-klient-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './client-list.component.html',
  styleUrl: './client-list.component.css'
})
export class KlientListComponent implements OnInit {
  klienci: Klient[] = [];
  loading = true;
  error = '';

  constructor(private klientService: KlientService) { }

  ngOnInit(): void {
    this.loadKlienci();
  }

  loadKlienci(): void {
    this.loading = true;
    this.klientService.getAllKlienci().subscribe({
      next: (data) => {
        this.klienci = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Błąd podczas ładowania klientów';
        this.loading = false;
        console.error('Error loading klienci:', error);
      }
    });
  }

  deleteKlient(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć tego klienta?')) {
      this.klientService.deleteKlient(id).subscribe({
        next: () => {
          this.loadKlienci();
        },
        error: (error) => {
          alert('Błąd podczas usuwania klienta');
          console.error('Error deleting klient:', error);
        }
      });
    }
  }
}
