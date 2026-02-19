import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { MovieService } from '../../services/movie.service';
import { Movie, Person } from '../../models/movie.model';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movie-detail.component.html',
  styleUrl: './movie-detail.component.scss'
})
export class MovieDetailComponent implements OnInit {
  movie: Movie | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private movieService: MovieService
  ) {}

  ngOnInit() {
    this.loadMovie();
  }

  loadMovie() {
    const movieId = this.route.snapshot.paramMap.get('id');
    if (!movieId) {
      this.error = 'Movie ID not found';
      return;
    }

    this.loading = true;
    this.error = null;

    this.movieService.getMovieById(movieId).subscribe({
      next: (movie) => {
        this.movie = movie;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load movie details. Please try again later.';
        this.loading = false;
        console.error('Error loading movie:', err);
      }
    });
  }

  goBack() {
    this.router.navigate(['/movies']);
  }

  rateMovie() {}

  addToWatchlist() {}

  getActorNames(): string[] {
    if (!this.movie || !this.movie.actors) return [];
    if (Array.isArray(this.movie.actors)) {
      return this.movie.actors.map((actor: any) =>
        typeof actor === 'string' ? actor : (actor as Person).name
      );
    }
    return [];
  }

  getDirectorNames(): string[] {
    if (!this.movie || !this.movie.directors) return [];
    if (Array.isArray(this.movie.directors)) {
      return this.movie.directors.map((director: any) =>
        typeof director === 'string' ? director : (director as Person).name
      );
    }
    return [];
  }
}
