import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CarService } from '../../../services/car.service';
import { Car } from '../../../models/car.model';

@Component({
  selector: 'app-samochod-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './car-list.component.html',
  styleUrl: './car-list.component.css'
})
export class CarListComponent implements OnInit {
  cars: Car[] = [];
  loading = true;
  error = '';

  constructor(private carService: CarService) { }

  ngOnInit(): void {
    this.loadCar();
  }

  loadCar(): void {
    this.loading = true;
    this.carService.getAllCars().subscribe({
      next: (data) => {
        this.cars = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Błąd podczas ładowania samochodów';
        this.loading = false;
        console.error('Error loading cars:', error);
      }
    });
  }

  deleteCar(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć ten samochód?')) {
      this.carService.deleteCar(id).subscribe({
        next: () => {
          this.loadCar();
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
