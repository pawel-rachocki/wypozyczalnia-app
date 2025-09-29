import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormControl, Validators, ReactiveFormsModule, FormsModule, AbstractControl } from '@angular/forms';
import { Rental, RentalRequest } from '../../models/rental.model';
import { Car } from '../../models/car.model';
import { Client } from '../../models/client.model';
import { RentalService } from '../../services/rental.service';
import { CarService } from '../../services/car.service';
import { ClientService } from '../../services/client.service';
import { forkJoin } from 'rxjs';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-rentals',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './rental.component.html',
  styleUrls: ['./rental.component.css']
})
export class WypozyczenieComponent implements OnInit {

  // Data properties
  rentals: Rental[] = [];
  activeRentals: Rental[] = [];
  availableCars: Car[] = [];
  clients: Client[] = [];

  rentalForm!: FormGroup;
  isLoading = false;
  error = '';
  success = '';
  showNewRentalForm = false;
  activeTab: 'active' | 'all' | 'new' = 'active';

  filterStatus = '';
  filterClient = '';
  sortBy: 'data' | 'klient' | 'samochod' | 'koszt' = 'data';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(
    private fb: FormBuilder,
    private rentalService: RentalService,
    private carService: CarService,
    private clientService: ClientService,
    private route: ActivatedRoute,
  ) {
    this.rentalForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadInitialData();
    this.checkForPreselectedCar();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      clientId: ['', [Validators.required]],
      carId: ['', [Validators.required]],
      rentalDate: [this.getCurrentDate(), [Validators.required]],
      plannedReturnDate: ['', [Validators.required]]
    });
  }
  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-CA');
  }

  private loadInitialData(): void {
    this.isLoading = true;

    forkJoin({
      rentals: this.rentalService.getAllRentals(),
      activeRentals: this.rentalService.getActiveRentals(),
      cars: this.carService.getAvailableCars(),
      clients: this.clientService.getAllClients()
    }).subscribe({
      next: (data) => {
        this.rentals = data.rentals;
        this.activeRentals = data.activeRentals;
        this.availableCars = data.cars;
        this.clients = data.clients;
        this.isLoading = false;
      },
      error: (error) => {
        this.handleError('Błąd podczas ładowania danych', error);
        this.isLoading = false;
      }
    });
  }

  onSubmitRental(): void {
    if (this.rentalForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const request: RentalRequest = this.rentalForm.value;

      if (!this.isValidDateRange(request.rentalDate, request.plannedReturnDate)) {
        this.error = 'Data zwrotu nie może być wcześniejsza niż data wypożyczenia';
        this.isLoading = false;
        return;
      }

      this.rentalService.rentCar(request).subscribe({
        next: (wypozyczenie) => {
          this.success = `Pomyślnie wypożyczono samochód ${wypozyczenie.car.brand} ${wypozyczenie.car.model}`;
          this.resetForm();
          this.refreshData();
          this.showNewRentalForm = false;
          this.activeTab = 'active';
        },
        error: (error) => {
          this.handleError('Błąd podczas wypożyczania samochodu', error);
        }
      });
    } else {
      this.markFormGroupTouched();
      this.error = 'Proszę wypełnić wszystkie wymagane pola';
    }
  }

  returnCar(wypozyczenie: Rental): void {
    if (!wypozyczenie.id) return;

    this.isLoading = true;
    this.clearMessages();

    this.rentalService.returnCar(wypozyczenie.id).subscribe({
      next: (updatedWypozyczenie) => {
        this.success = `Pomyślnie zwrócono samochód ${updatedWypozyczenie.car.brand} ${updatedWypozyczenie.car.model}`;
        this.refreshData();
      },
      error: (error) => {
        this.handleError('Błąd podczas zwrotu samochodu', error);
      }
    });
  }

  private refreshData(): void {
    forkJoin({
      rentals: this.rentalService.getAllRentals(),
      activeRentals: this.rentalService.getActiveRentals(),
      cars: this.carService.getAvailableCars()
    }).subscribe({
      next: (data) => {
        this.rentals = data.rentals;
        this.activeRentals = data.activeRentals;
        this.availableCars = data.cars;
        this.isLoading = false;
      },
      error: (error) => {
        this.handleError('Błąd podczas odświeżania danych', error);
        this.isLoading = false;
      }
    });
  }

  get filteredRentals(): Rental[] {
    let filtered = this.rentals;

    if (this.filterStatus) {
      filtered = filtered.filter(w => w.status === this.filterStatus);
    }

    if (this.filterClient) {
      const searchTerm = this.filterClient.toLowerCase();
      filtered = filtered.filter(w =>
        w.client.firstName.toLowerCase().includes(searchTerm) ||
        w.client.lastName.toLowerCase().includes(searchTerm) ||
        w.client.email.toLowerCase().includes(searchTerm)
      );
    }

    return this.sortRentals(filtered);
  }

  private sortRentals(rentals: Rental[]): Rental[] {
    return rentals.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'data':
          comparison = new Date(a.rentalDate).getTime() - new Date(b.rentalDate).getTime();
          break;
        case 'klient':
          comparison = (a.client.lastName + a.client.firstName).localeCompare(b.client.lastName + b.client.firstName);
          break;
        case 'samochod':
          comparison = (a.car.brand + a.car.model).localeCompare(b.car.brand + b.car.model);
          break;
        case 'koszt':
          comparison = a.totalCost - b.totalCost;
          break;
      }

      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  onSortChange(field: 'data' | 'klient' | 'samochod' | 'koszt'): void {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'asc';
    }
  }

  private resetForm(): void {
    this.rentalForm.reset();
    this.rentalForm.patchValue({
      rentalDate: this.getCurrentDate()
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.rentalForm.controls).forEach(key => {
      const control = this.rentalForm.get(key);
      if (control) {
        (control as any).markAsTouched();
      }
    });
  }

  private isValidDateRange(startDate: string, endDate: string): boolean {
    return new Date(startDate) <= new Date(endDate);
  }

  private handleError(message: string, error: any): void {
    console.error(message, error);
    this.error = message;
    if (error.error?.message) {
      this.error += ': ' + error.error.message;
    }
    this.isLoading = false;
  }

  private clearMessages(): void {
    this.error = '';
    this.success = '';
  }

  getClientFullName(klient: Client): string {
    return `${klient.firstName} ${klient.lastName}`;
  }

  getCarFullName(samochod: Car): string {
    return `${samochod.brand} ${samochod.model}`;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'AKTYWNE': return 'status-active';
      case 'ZAKONCZONE': return 'status-completed';
      case 'PRZETERMINOWANE': return 'status-overdue';
      case 'ANULOWANE': return 'status-cancelled';
      default: return 'status-default';
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.rentalForm.get(fieldName);
    return field ? field.invalid && ((field as any).dirty || (field as any).touched) : false;
  }

  getFieldError(fieldName: string): string {
    const field = this.rentalForm.get(fieldName);
    if (field && (field as any).errors) {
      if ((field as any).errors['required']) return `${fieldName} jest wymagane`;
    }
    return '';
  }

  setActiveTab(tab: 'active' | 'all' | 'new'): void {
    this.activeTab = tab;
    this.clearMessages();

    if (tab === 'new') {
      this.showNewRentalForm = true;
    } else {
      this.showNewRentalForm = false;
    }
  }

  get isClientValid(): boolean {
    const field: AbstractControl | null = this.rentalForm.get('clientId');
    return field ? field.valid : false;
  }

  get isCarValid(): boolean {
    const field: AbstractControl | null = this.rentalForm.get('carId');
    return field ? field.valid : false;
  }

  get isDateValid(): boolean {
    const rentalDate = this.rentalForm.get('rentalDate')?.value;
    const plannedReturnDate = this.rentalForm.get('plannedReturnDate')?.value;
    return !!(rentalDate && plannedReturnDate && this.isValidDateRange(rentalDate, plannedReturnDate));
  }

  get estimatedCost(): number {
    const carId = this.rentalForm.get('carId')?.value;
    const rentalDate = this.rentalForm.get('rentalDate')?.value;
    const plannedReturnDate = this.rentalForm.get('plannedReturnDate')?.value;

    if (!carId || !rentalDate || !plannedReturnDate) {
      return 0;
    }

    const samochod = this.availableCars.find(s => s.id === +carId);
    if (!samochod) return 0;

    const startDate = new Date(rentalDate);
    const endDate = new Date(plannedReturnDate);
    const daysDiff = Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));

    return daysDiff * samochod.dailyPrice;
  }

  getDaysDifference(): number {
    const rentalDate = this.rentalForm.get('rentalDate')?.value;
    const plannedReturnDate = this.rentalForm.get('plannedReturnDate')?.value;

    if (!rentalDate || !plannedReturnDate) return 0;

    const startDate = new Date(rentalDate);
    const endDate = new Date(plannedReturnDate);
    return Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  getSelectedCarPrice(): number {
    const carId = this.rentalForm.get('carId')?.value;
    if (!carId) return 0;

    const samochod = this.availableCars.find(s => s.id === +carId);
    return samochod ? samochod.dailyPrice : 0;
  }
  shouldShowValidationSummary(): boolean {
    return this.rentalForm.invalid &&
      ((this.rentalForm as any).dirty || (this.rentalForm as any).touched);
  }

  private checkForPreselectedCar(): void {
    const carId = this.route.snapshot.paramMap.get('carId');
    if (carId) {
      this.setActiveTab('new');

      if (this.availableCars.length > 0) {
        this.preselectCar(carId);
      } else {
        setTimeout(() => this.preselectCar(carId), 500);
      }
    }
  }

  private preselectCar(carId: string): void {
    const carExists = this.availableCars.find(s => s.id === +carId);
    if (carExists) {
      this.rentalForm.patchValue({
        carId: +carId
      });
      this.success = `Wybrany samochód: ${carExists.brand} ${carExists.model}`;
    }
  }
}
