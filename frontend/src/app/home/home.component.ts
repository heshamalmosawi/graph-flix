import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="home-container">
      <h1>Welcome to GraphFlix</h1>
      <p>Your movie recommendation engine.</p>
    </div>
  `,
  styles: [`
    .home-container {
      text-align: center;
      padding: 2rem;
      color: var(--text-primary);
    }
    h1 {
      font-size: 2.5rem;
      color: var(--color-primary);
      margin-bottom: 1rem;
    }
  `]
})
export class HomeComponent {}
