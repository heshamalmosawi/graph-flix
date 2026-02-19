import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MovieService } from '../../services/movie.service';
import { SearchBarComponent } from '../search-bar/search-bar.component';
import { MovieCardComponent } from '../movie-card/movie-card.component';
import { Movie, PagedMovieResponse } from '../../models/movie.model';

@Component({
  selector: 'app-movie-search',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, MovieCardComponent],
  templateUrl: './movie-search.component.html',
  styleUrl: './movie-search.component.scss'
})
export class MovieSearchPage implements OnInit {
  movies: Movie[] = [];
  pagedResponse: PagedMovieResponse | null = null;
  loading = false;
  error: string | null = null;
  currentPage = 0;
  pageSize = 20;
  currentSearchRequest: any = {};

  get totalPages(): number {
    return this.pagedResponse?.totalPages || 1;
  }

  constructor(private movieService: MovieService) {}

  ngOnInit() {}

  onSearch(searchRequest: any) {
    this.currentSearchRequest = searchRequest;
    this.currentPage = 0;
    this.searchMovies();
  }

  searchMovies() {
    this.loading = true;
    this.error = null;

    const title = this.currentSearchRequest.title;
    const person = this.currentSearchRequest.person;

    if (title) {
      this.movieService.searchByTitle(title).subscribe({
        next: (movies) => {
          this.movies = movies;
          this.pagedResponse = {
            content: movies,
            totalElements: movies.length,
            totalPages: 1,
            number: this.currentPage,
            size: this.pageSize,
            first: this.currentPage === 0,
            last: true
          };
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to search movies. Please try again later.';
          this.loading = false;
          console.error('Error searching movies:', err);
        }
      });
    } else if (person) {
      this.movieService.searchByPerson(person).subscribe({
        next: (movies) => {
          this.movies = movies;
          this.pagedResponse = {
            content: movies,
            totalElements: movies.length,
            totalPages: 1,
            number: this.currentPage,
            size: this.pageSize,
            first: this.currentPage === 0,
            last: true
          };
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to search movies. Please try again later.';
          this.loading = false;
          console.error('Error searching movies:', err);
        }
      });
    } else {
      this.movieService.getAllMovies().subscribe({
        next: (response) => {
          this.movies = response.content;
          this.pagedResponse = response;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load movies. Please try again later.';
          this.loading = false;
          console.error('Error loading movies:', err);
        }
      });
    }
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.searchMovies();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
