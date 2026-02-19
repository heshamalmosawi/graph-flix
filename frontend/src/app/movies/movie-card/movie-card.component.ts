import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { Movie } from '../../models/movie.model';

@Component({
  selector: 'app-movie-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movie-card.component.html',
  styleUrl: './movie-card.component.scss'
})
export class MovieCardComponent {
  @Input() movie!: Movie;

  constructor(private router: Router) {}

  onMovieClick() {
    this.router.navigate(['/movies', this.movie.id]);
  }

  getDirectorsCount(): number {
    if (!this.movie.directors) return 0;
    return this.movie.directors.length;
  }

  getActorsCount(): number {
    if (!this.movie.actors) return 0;
    return this.movie.actors.length;
  }
}
