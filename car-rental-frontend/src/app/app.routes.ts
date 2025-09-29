import { Routes } from '@angular/router';
import { SamochodListComponent } from './components/samochody/samochod-list/car-list.component';
import { SamochodFormComponent } from './components/samochody/samochod-form/car-form.component';
import { KlientListComponent } from './components/klienci/client-list/client-list.component';
import { KlientFormComponent } from './components/klienci/client-form/client-form.component';
import {WypozyczenieComponent} from './components/wypozyczenia/rental.component';

export const routes: Routes = [
  { path: '', redirectTo: '/samochody', pathMatch: 'full' },
  { path: 'samochody', component: SamochodListComponent },
  { path: 'samochody/new', component: SamochodFormComponent },
  { path: 'samochody/edit/:id', component: SamochodFormComponent },
  { path: 'klienci', component: KlientListComponent },
  { path: 'klienci/new', component: KlientFormComponent },
  { path: 'klienci/edit/:id', component: KlientFormComponent },
  { path: 'wypozyczenia', component: WypozyczenieComponent },
  { path: 'wypozyczenia/new/:carId', component: WypozyczenieComponent }
];
