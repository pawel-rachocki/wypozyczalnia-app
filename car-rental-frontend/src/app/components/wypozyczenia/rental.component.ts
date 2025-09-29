import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormControl, Validators, ReactiveFormsModule, FormsModule, AbstractControl } from '@angular/forms';
import { Wypozyczenie, WypozyczenieRequest } from '../../models/rental.model';
import { Samochod } from '../../models/car.model';
import { Klient } from '../../models/client.model';
import { WypozyczenieService } from '../../services/rental.service';
import { SamochodService } from '../../services/car.service';
import { KlientService } from '../../services/client.service';
import { forkJoin } from 'rxjs';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-wypozyczenia',
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
  wypozyczenia: Wypozyczenie[] = [];
  activeRentals: Wypozyczenie[] = [];
  dostepneSamochody: Samochod[] = [];
  klienci: Klient[] = [];

  wypozyczenieForm!: FormGroup;
  isLoading = false;
  error = '';
  success = '';
  showNewRentalForm = false;
  activeTab: 'active' | 'all' | 'new' = 'active';

  filterStatus = '';
  filterKlient = '';
  sortBy: 'data' | 'klient' | 'samochod' | 'koszt' = 'data';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(
    private fb: FormBuilder,
    private wypozyczenieService: WypozyczenieService,
    private samochodService: SamochodService,
    private klientService: KlientService,
    private route: ActivatedRoute,
  ) {
    this.wypozyczenieForm = this.createForm();
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
      wypozyczenia: this.wypozyczenieService.getAllRentals(),
      activeRentals: this.wypozyczenieService.getActiveRentals(),
      samochody: this.samochodService.getAvailableSamochody(),
      klienci: this.klientService.getAllKlienci()
    }).subscribe({
      next: (data) => {
        this.wypozyczenia = data.wypozyczenia;
        this.activeRentals = data.activeRentals;
        this.dostepneSamochody = data.samochody;
        this.klienci = data.klienci;
        this.isLoading = false;
      },
      error: (error) => {
        this.handleError('Błąd podczas ładowania danych', error);
        this.isLoading = false;
      }
    });
  }

  onSubmitRental(): void {
    if (this.wypozyczenieForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const request: WypozyczenieRequest = this.wypozyczenieForm.value;

      if (!this.isValidDateRange(request.rentalDate, request.plannedReturnDate)) {
        this.error = 'Data zwrotu nie może być wcześniejsza niż data wypożyczenia';
        this.isLoading = false;
        return;
      }

      this.wypozyczenieService.rentCar(request).subscribe({
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

  returnCar(wypozyczenie: Wypozyczenie): void {
    if (!wypozyczenie.id) return;

    this.isLoading = true;
    this.clearMessages();

    this.wypozyczenieService.returnCar(wypozyczenie.id).subscribe({
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
      wypozyczenia: this.wypozyczenieService.getAllRentals(),
      activeRentals: this.wypozyczenieService.getActiveRentals(),
      samochody: this.samochodService.getAvailableSamochody()
    }).subscribe({
      next: (data) => {
        this.wypozyczenia = data.wypozyczenia;
        this.activeRentals = data.activeRentals;
        this.dostepneSamochody = data.samochody;
        this.isLoading = false;
      },
      error: (error) => {
        this.handleError('Błąd podczas odświeżania danych', error);
        this.isLoading = false;
      }
    });
  }

  get filteredWypozyczenia(): Wypozyczenie[] {
    let filtered = this.wypozyczenia;

    if (this.filterStatus) {
      filtered = filtered.filter(w => w.status === this.filterStatus);
    }

    if (this.filterKlient) {
      const searchTerm = this.filterKlient.toLowerCase();
      filtered = filtered.filter(w =>
        w.client.firstName.toLowerCase().includes(searchTerm) ||
        w.client.lastName.toLowerCase().includes(searchTerm) ||
        w.client.email.toLowerCase().includes(searchTerm)
      );
    }

    return this.sortWypozyczenia(filtered);
  }

  private sortWypozyczenia(wypozyczenia: Wypozyczenie[]): Wypozyczenie[] {
    return wypozyczenia.sort((a, b) => {
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
    this.wypozyczenieForm.reset();
    this.wypozyczenieForm.patchValue({
      rentalDate: this.getCurrentDate()
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.wypozyczenieForm.controls).forEach(key => {
      const control = this.wypozyczenieForm.get(key);
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

  getKlientFullName(klient: Klient): string {
    return `${klient.firstName} ${klient.lastName}`;
  }

  getSamochodFullName(samochod: Samochod): string {
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
    const field = this.wypozyczenieForm.get(fieldName);
    return field ? field.invalid && ((field as any).dirty || (field as any).touched) : false;
  }

  getFieldError(fieldName: string): string {
    const field = this.wypozyczenieForm.get(fieldName);
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

  get isKlientValid(): boolean {
    const field: AbstractControl | null = this.wypozyczenieForm.get('clientId');
    return field ? field.valid : false;
  }

  get isSamochodValid(): boolean {
    const field: AbstractControl | null = this.wypozyczenieForm.get('carId');
    return field ? field.valid : false;
  }

  get isDateValid(): boolean {
    const rentalDate = this.wypozyczenieForm.get('rentalDate')?.value;
    const plannedReturnDate = this.wypozyczenieForm.get('plannedReturnDate')?.value;
    return !!(rentalDate && plannedReturnDate && this.isValidDateRange(rentalDate, plannedReturnDate));
  }

  get estimatedCost(): number {
    const carId = this.wypozyczenieForm.get('carId')?.value;
    const rentalDate = this.wypozyczenieForm.get('rentalDate')?.value;
    const plannedReturnDate = this.wypozyczenieForm.get('plannedReturnDate')?.value;

    if (!carId || !rentalDate || !plannedReturnDate) {
      return 0;
    }

    const samochod = this.dostepneSamochody.find(s => s.id === +carId);
    if (!samochod) return 0;

    const startDate = new Date(rentalDate);
    const endDate = new Date(plannedReturnDate);
    const daysDiff = Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));

    return daysDiff * samochod.dailyPrice;
  }

  getDaysDifference(): number {
    const rentalDate = this.wypozyczenieForm.get('rentalDate')?.value;
    const plannedReturnDate = this.wypozyczenieForm.get('plannedReturnDate')?.value;

    if (!rentalDate || !plannedReturnDate) return 0;

    const startDate = new Date(rentalDate);
    const endDate = new Date(plannedReturnDate);
    return Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  getSelectedCarPrice(): number {
    const carId = this.wypozyczenieForm.get('carId')?.value;
    if (!carId) return 0;

    const samochod = this.dostepneSamochody.find(s => s.id === +carId);
    return samochod ? samochod.dailyPrice : 0;
  }
  shouldShowValidationSummary(): boolean {
    return this.wypozyczenieForm.invalid &&
      ((this.wypozyczenieForm as any).dirty || (this.wypozyczenieForm as any).touched);
  }

  private checkForPreselectedCar(): void {
    const carId = this.route.snapshot.paramMap.get('carId');
    if (carId) {
      this.setActiveTab('new');

      if (this.dostepneSamochody.length > 0) {
        this.preselectCar(carId);
      } else {
        setTimeout(() => this.preselectCar(carId), 500);
      }
    }
  }

  private preselectCar(carId: string): void {
    const carExists = this.dostepneSamochody.find(s => s.id === +carId);
    if (carExists) {
      this.wypozyczenieForm.patchValue({
        carId: +carId
      });
      this.success = `Wybrany samochód: ${carExists.brand} ${carExists.model}`;
    }
  }
}
