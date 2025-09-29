import { Routes } from '@angular/router';
import { CarListComponent } from './components/samochody/samochod-list/car-list.component';
import { CarFormComponent } from './components/samochody/samochod-form/car-form.component';
import { ClientListComponent } from './components/klienci/client-list/client-list.component';
import { KlientFormComponent } from './components/klienci/client-form/client-form.component';
import {WypozyczenieComponent} from './components/wypozyczenia/rental.component';

export const routes: Routes = [
  { path: '', redirectTo: '/samochody', pathMatch: 'full' },
  { path: 'samochody', component: CarListComponent },
  { path: 'samochody/new', component: CarFormComponent },
  { path: 'samochody/edit/:id', component: CarFormComponent },
  { path: 'klienci', component: ClientListComponent },
  { path: 'klienci/new', component: KlientFormComponent },
  { path: 'klienci/edit/:id', component: KlientFormComponent },
  { path: 'wypozyczenia', component: WypozyczenieComponent },
  { path: 'wypozyczenia/new/:carId', component: WypozyczenieComponent }
];
