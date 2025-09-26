import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SamochodService } from '../../../services/samochod.service';
import { Samochod } from '../../../models/samochod.model';

@Component({
  selector: 'app-samochod-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './samochod-list.component.html',
  styleUrl: './samochod-list.component.css'
})
export class SamochodListComponent implements OnInit {
  samochody: Samochod[] = [];
  loading = true;
  error = '';

  constructor(private samochodService: SamochodService) { }

  ngOnInit(): void {
    this.loadSamochody();
  }

  loadSamochody(): void {
    this.loading = true;
    this.samochodService.getAllSamochody().subscribe({
      next: (data) => {
        this.samochody = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Błąd podczas ładowania samochodów';
        this.loading = false;
        console.error('Error loading samochody:', error);
      }
    });
  }

  deleteSamochod(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć ten samochód?')) {
      this.samochodService.deleteSamochod(id).subscribe({
        next: () => {
          this.loadSamochody();
        },
        error: (error) => {
          alert('Błąd podczas usuwania samochodu');
          console.error('Error deleting samochod:', error);
        }
      });
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'DOSTEPNY':
        return 'badge bg-success';
      case 'WYPOZYCZONY':
        return 'badge bg-danger';
      default:
        return 'badge bg-secondary';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'DOSTEPNY':
        return 'Dostępny';
      case 'WYPOZYCZONY':
        return 'Wypożyczony';
      default:
        return status;
    }
  }
}
