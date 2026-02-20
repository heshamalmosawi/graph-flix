import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MovieService } from '../services/movie.service';
import { MovieCardComponent } from '../movies/movie-card/movie-card.component';
import { Movie, PagedMovieResponse } from '../models/movie.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, MovieCardComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  movies: Movie[] = [];
  pagedResponse: PagedMovieResponse | null = null;
  loading = false;
  error: string | null = null;
  currentPage = 0;
  pageSize = 20;

  get totalPages(): number {
    return this.pagedResponse?.totalPages || 1;
  }

  constructor(private movieService: MovieService) {}

  ngOnInit() {
    this.loadMovies();
  }

  loadMovies() {
    this.loading = true;
    this.error = null;

    this.movieService.getAllMovies(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.pagedResponse = response;
        this.movies = response.content;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load movies. Please try again later.';
        this.loading = false;
        console.error('Error loading movies:', err);
      }
    });
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadMovies();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
