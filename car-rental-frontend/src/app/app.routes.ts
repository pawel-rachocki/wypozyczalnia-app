import { Routes } from '@angular/router';
import {SamochodListComponent} from './components/samochody/samochod-list/samochod-list.component';
import {SamochodFormComponent} from './components/samochody/samochod-form/samochod-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/samochody', pathMatch: 'full' },
  { path: 'samochody', component: SamochodListComponent },
  { path: 'samochody/new', component: SamochodFormComponent },
  { path: 'samochody/edit/:id', component: SamochodFormComponent }
];
