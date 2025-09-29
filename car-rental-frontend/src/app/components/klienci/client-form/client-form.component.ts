import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { KlientService } from '../../../services/client.service';
import { Klient } from '../../../models/client.model';

@Component({
  selector: 'app-klient-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.css'
})
export class KlientFormComponent implements OnInit {
  klientForm: FormGroup;
  isEditMode = false;
  klientId: number | null = null;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private klientService: KlientService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.klientForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.klientId = +id;
      this.loadKlient(this.klientId);
    }
  }

  loadKlient(id: number): void {
    this.loading = true;
    this.klientService.getKlientById(id).subscribe({
      next: (klient) => {
        this.klientForm.patchValue(klient);
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Błąd podczas ładowania danych klienta';
        this.loading = false;
        console.error('Error loading klient:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.klientForm.valid) {
      this.loading = true;
      const klientData: Klient = this.klientForm.value;

      const operation = this.isEditMode
        ? this.klientService.updateKlient(this.klientId!, klientData)
        : this.klientService.createKlient(klientData);

      operation.subscribe({
        next: () => {
          this.router.navigate(['/klienci']);
        },
        error: (error) => {
          // ZMIEŃ TO - dodaj więcej szczegółów
          console.error('Full error object:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.error);
          console.error('Sent data:', klientData);

          this.error = error.error?.message || 'Błąd podczas zapisywania klienta';
          this.loading = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.router.navigate(['/klienci']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.klientForm.controls).forEach(key => {
      const control = this.klientForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.klientForm.get(fieldName);
    if (field?.errors && (field as any).touched) {
      if (field.errors['required']) return `${fieldName} jest wymagane`;
      if (field.errors['minlength']) return `${fieldName} musi mieć co najmniej ${field.errors['minlength'].requiredLength} znaków`;
      if (field.errors['maxlength']) return `${fieldName} może mieć maksymalnie ${field.errors['maxlength'].requiredLength} znaków`;
      if (field.errors['email']) return 'Email ma nieprawidłowy format';
    }
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.klientForm.get(fieldName);
    return field ? (field.invalid && (field as any).touched) : false;
  }
}
