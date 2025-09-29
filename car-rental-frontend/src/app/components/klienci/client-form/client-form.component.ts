import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClientService } from '../../../services/client.service';
import { Client } from '../../../models/client.model';

@Component({
  selector: 'app-klient-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.css'
})
export class KlientFormComponent implements OnInit {
  clientForm: FormGroup;
  isEditMode = false;
  clientId: number | null = null;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.clientForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.clientId = +id;
      this.loadKlient(this.clientId);
    }
  }

  loadKlient(id: number): void {
    this.loading = true;
    this.clientService.getClientById(id).subscribe({
      next: (klient) => {
        this.clientForm.patchValue(klient);
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
    if (this.clientForm.valid) {
      this.loading = true;
      const klientData: Client = this.clientForm.value;

      const operation = this.isEditMode
        ? this.clientService.updateClient(this.clientId!, klientData)
        : this.clientService.createClient(klientData);

      operation.subscribe({
        next: () => {
          this.router.navigate(['/klienci']);
        },
        error: (error) => {
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
    Object.keys(this.clientForm.controls).forEach(key => {
      const control = this.clientForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.clientForm.get(fieldName);
    if (field?.errors && (field as any).touched) {
      if (field.errors['required']) return `${fieldName} jest wymagane`;
      if (field.errors['minlength']) return `${fieldName} musi mieć co najmniej ${field.errors['minlength'].requiredLength} znaków`;
      if (field.errors['maxlength']) return `${fieldName} może mieć maksymalnie ${field.errors['maxlength'].requiredLength} znaków`;
      if (field.errors['email']) return 'Email ma nieprawidłowy format';
    }
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.clientForm.get(fieldName);
    return field ? (field.invalid && (field as any).touched) : false;
  }
}
