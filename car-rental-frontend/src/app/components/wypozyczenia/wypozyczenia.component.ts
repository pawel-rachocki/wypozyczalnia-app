import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormControl, Validators, ReactiveFormsModule, FormsModule, AbstractControl } from '@angular/forms';
import { Wypozyczenie, WypozyczenieRequest } from '../../models/wypozyczenie.model';
import { Samochod } from '../../models/samochod.model';
import { Klient } from '../../models/klient.model';
import { WypozyczenieService } from '../../services/wypozyczenie.service';
import { SamochodService } from '../../services/samochod.service';
import { KlientService } from '../../services/klient.service';
import { forkJoin } from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-wypozyczenia',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './wypozyczenia.component.html',
  styleUrls: ['./wypozyczenia.component.css']
})
export class WypozyczenieComponent implements OnInit {

  // Data properties
  wypozyczenia: Wypozyczenie[] = [];
  activeRentals: Wypozyczenie[] = [];
  dostepneSamochody: Samochod[] = [];
  klienci: Klient[] = [];

  // Form and UI state
  wypozyczenieForm!: FormGroup;
  isLoading = false;
  error = '';
  success = '';
  showNewRentalForm = false;
  activeTab: 'active' | 'all' | 'new' = 'active';

  // Filter properties
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
    private router: Router
  ) {
    this.wypozyczenieForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadInitialData();
    this.checkForPreselectedCar();
  }

  // ===== INITIALIZATION =====

  private createForm(): FormGroup {
    return this.fb.group({
      klientId: ['', [Validators.required]],
      samochodId: ['', [Validators.required]],
      dataWypozyczenia: [this.getCurrentDate(), [Validators.required]],
      planowanaDataZwrotu: ['', [Validators.required]]
    });
  }

  // ===== UTILITY METHODS =====

  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-CA');
  }

  private loadInitialData(): void {
    this.isLoading = true;

    forkJoin({
      wypozyczenia: this.wypozyczenieService.getAllWypozyczenia(),
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

  // ===== RENTAL OPERATIONS =====

  onSubmitRental(): void {
    if (this.wypozyczenieForm.valid) {
      this.isLoading = true;
      this.clearMessages();

      const request: WypozyczenieRequest = this.wypozyczenieForm.value;

      // Validate dates
      if (!this.isValidDateRange(request.dataWypozyczenia, request.planowanaDataZwrotu)) {
        this.error = 'Data zwrotu nie może być wcześniejsza niż data wypożyczenia';
        this.isLoading = false;
        return;
      }

      this.wypozyczenieService.rentCar(request).subscribe({
        next: (wypozyczenie) => {
          this.success = `Pomyślnie wypożyczono samochód ${wypozyczenie.samochod.marka} ${wypozyczenie.samochod.model}`;
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

    const dataZwrotu = new Date().toISOString().split('T')[0];
    this.isLoading = true;
    this.clearMessages();

    this.wypozyczenieService.returnCar(wypozyczenie.id, dataZwrotu).subscribe({
      next: (updatedWypozyczenie) => {
        this.success = `Pomyślnie zwrócono samochód ${updatedWypozyczenie.samochod.marka} ${updatedWypozyczenie.samochod.model}`;
        this.refreshData();
      },
      error: (error) => {
        this.handleError('Błąd podczas zwrotu samochodu', error);
      }
    });
  }

  // ===== DATA MANAGEMENT =====

  private refreshData(): void {
    forkJoin({
      wypozyczenia: this.wypozyczenieService.getAllWypozyczenia(),
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

  // ===== FILTERING AND SORTING =====

  get filteredWypozyczenia(): Wypozyczenie[] {
    let filtered = this.wypozyczenia;

    if (this.filterStatus) {
      filtered = filtered.filter(w => w.status === this.filterStatus);
    }

    if (this.filterKlient) {
      const searchTerm = this.filterKlient.toLowerCase();
      filtered = filtered.filter(w =>
        w.klient.imie.toLowerCase().includes(searchTerm) ||
        w.klient.nazwisko.toLowerCase().includes(searchTerm) ||
        w.klient.email.toLowerCase().includes(searchTerm)
      );
    }

    return this.sortWypozyczenia(filtered);
  }

  private sortWypozyczenia(wypozyczenia: Wypozyczenie[]): Wypozyczenie[] {
    return wypozyczenia.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'data':
          comparison = new Date(a.dataWypozyczenia).getTime() - new Date(b.dataWypozyczenia).getTime();
          break;
        case 'klient':
          comparison = (a.klient.nazwisko + a.klient.imie).localeCompare(b.klient.nazwisko + b.klient.imie);
          break;
        case 'samochod':
          comparison = (a.samochod.marka + a.samochod.model).localeCompare(b.samochod.marka + b.samochod.model);
          break;
        case 'koszt':
          comparison = a.kosztCalkowity - b.kosztCalkowity;
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

  // ===== UTILITY METHODS =====

  private resetForm(): void {
    this.wypozyczenieForm.reset();
    this.wypozyczenieForm.patchValue({
      dataWypozyczenia: this.getCurrentDate()
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

  // ===== UI HELPERS =====

  getKlientFullName(klient: Klient): string {
    return `${klient.imie} ${klient.nazwisko}`;
  }

  getSamochodFullName(samochod: Samochod): string {
    return `${samochod.marka} ${samochod.model}`;
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
      // Add more validation messages as needed
    }
    return '';
  }

  // ===== TAB MANAGEMENT =====

  setActiveTab(tab: 'active' | 'all' | 'new'): void {
    this.activeTab = tab;
    this.clearMessages();

    if (tab === 'new') {
      this.showNewRentalForm = true;
    } else {
      this.showNewRentalForm = false;
    }
  }

  // ===== FORM VALIDATION HELPERS =====

  get isKlientValid(): boolean {
    const field: AbstractControl | null = this.wypozyczenieForm.get('klientId');
    return field ? field.valid : false;
  }

  get isSamochodValid(): boolean {
    const field: AbstractControl | null = this.wypozyczenieForm.get('samochodId');
    return field ? field.valid : false;
  }

  get isDateValid(): boolean {
    const dataWypozyczenia = this.wypozyczenieForm.get('dataWypozyczenia')?.value;
    const planowanaDataZwrotu = this.wypozyczenieForm.get('planowanaDataZwrotu')?.value;
    return !!(dataWypozyczenia && planowanaDataZwrotu && this.isValidDateRange(dataWypozyczenia, planowanaDataZwrotu));
  }

  // Calculate estimated cost
  get estimatedCost(): number {
    const samochodId = this.wypozyczenieForm.get('samochodId')?.value;
    const dataWypozyczenia = this.wypozyczenieForm.get('dataWypozyczenia')?.value;
    const planowanaDataZwrotu = this.wypozyczenieForm.get('planowanaDataZwrotu')?.value;

    if (!samochodId || !dataWypozyczenia || !planowanaDataZwrotu) {
      return 0;
    }

    const samochod = this.dostepneSamochody.find(s => s.id === +samochodId);
    if (!samochod) return 0;

    const startDate = new Date(dataWypozyczenia);
    const endDate = new Date(planowanaDataZwrotu);
    const daysDiff = Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));

    return daysDiff * samochod.cenaZaDzien;
  }

  // Helper methods for template
  getDaysDifference(): number {
    const dataWypozyczenia = this.wypozyczenieForm.get('dataWypozyczenia')?.value;
    const planowanaDataZwrotu = this.wypozyczenieForm.get('planowanaDataZwrotu')?.value;

    if (!dataWypozyczenia || !planowanaDataZwrotu) return 0;

    const startDate = new Date(dataWypozyczenia);
    const endDate = new Date(planowanaDataZwrotu);
    return Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  getSelectedCarPrice(): number {
    const samochodId = this.wypozyczenieForm.get('samochodId')?.value;
    if (!samochodId) return 0;

    const samochod = this.dostepneSamochody.find(s => s.id === +samochodId);
    return samochod ? samochod.cenaZaDzien : 0;
  }
  shouldShowValidationSummary(): boolean {
    return this.wypozyczenieForm.invalid &&
      ((this.wypozyczenieForm as any).dirty || (this.wypozyczenieForm as any).touched);
  }

  private checkForPreselectedCar(): void {
    const samochodId = this.route.snapshot.paramMap.get('samochodId');
    if (samochodId) {
      this.setActiveTab('new');

      if (this.dostepneSamochody.length > 0) {
        this.preselectCar(samochodId);
      } else {
        setTimeout(() => this.preselectCar(samochodId), 500);
      }
    }
  }

  private preselectCar(samochodId: string): void {
    const carExists = this.dostepneSamochody.find(s => s.id === +samochodId);
    if (carExists) {
      this.wypozyczenieForm.patchValue({
        samochodId: +samochodId
      });
      this.success = `Wybrany samochód: ${carExists.marka} ${carExists.model}`;
    }
  }
}
