import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { SamochodService } from '../../../services/car.service';
import { Samochod } from '../../../models/car.model';

@Component({
  selector: 'app-samochod-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './car-form.component.html',
  styleUrl: './car-form.component.css'
})
export class SamochodFormComponent implements OnInit {
  samochodForm: FormGroup;
  isEditMode = false;
  samochodId: number | null = null;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private samochodService: SamochodService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.samochodForm = this.fb.group({
      brand: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      model: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      dailyPrice: ['', [Validators.required, Validators.min(0.01), Validators.max(10000)]],
      status: ['DOSTEPNY', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.samochodId = +id;
      this.loadSamochod(this.samochodId);
    }
  }

  loadSamochod(id: number): void {
    this.loading = true;
    this.samochodService.getSamochodById(id).subscribe({
      next: (samochod) => {
        this.samochodForm.patchValue(samochod);
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Błąd podczas ładowania danych samochodu';
        this.loading = false;
        console.error('Error loading samochod:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.samochodForm.valid) {
      this.loading = true;
      const samochodData: Samochod = this.samochodForm.value;

      const operation = this.isEditMode
        ? this.samochodService.updateSamochod(this.samochodId!, samochodData)
        : this.samochodService.createSamochod(samochodData);

      operation.subscribe({
        next: () => {
          this.router.navigate(['/samochody']);
        },
        error: (error) => {
          this.error = 'Błąd podczas zapisywania samochodu';
          this.loading = false;
          console.error('Error saving samochod:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.router.navigate(['/samochody']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.samochodForm.controls).forEach(key => {
      const control = this.samochodForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.samochodForm.get(fieldName);
    if (field?.errors && (field as any).touched) {
      if (field.errors['required']) return `${fieldName} jest wymagane`;
      if (field.errors['minlength']) return `${fieldName} musi mieć co najmniej ${field.errors['minlength'].requiredLength} znaków`;
      if (field.errors['maxlength']) return `${fieldName} może mieć maksymalnie ${field.errors['maxlength'].requiredLength} znaków`;
      if (field.errors['min']) return `${fieldName} musi być większe od ${field.errors['min'].min}`;
      if (field.errors['max']) return `${fieldName} nie może być większe od ${field.errors['max'].max}`;
    }
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.samochodForm.get(fieldName);
    return field ? (field.invalid && (field as any).touched) : false;
  }
}
