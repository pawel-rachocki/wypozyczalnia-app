import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { SamochodService } from './services/samochod.service';
import {FooterComponent} from './components/shared/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    FooterComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'car-rental-frontend';

  constructor(private samochodService: SamochodService) {}

  ngOnInit() {
    this.samochodService.getAllSamochody().subscribe({
      next: (samochody) => {
        console.log('Połączenie z API działa:', samochody);
      },
      error: (error) => {
        console.error('Błąd połączenia z API:', error);
      }
    });
  }
}
