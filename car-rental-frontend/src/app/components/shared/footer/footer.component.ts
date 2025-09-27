import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule,
    RouterModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  authorName = 'Paweł Rachocki';
  authorEmail = 'pawel.rachocki@outlook.com';
  githubRepo = 'https://github.com/pawel-rachocki/wypozyczalnia-app';
  youtubeVideo = 'https://youtu.be/WFHuif5oxVI';

  openLink(url: string): void {
    window.open(url, '_blank');
  }
}
