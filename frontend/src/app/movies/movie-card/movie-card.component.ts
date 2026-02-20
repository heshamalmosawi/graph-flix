import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { Movie } from '../../models/movie.model';
import { WatchlistService } from '../../services/watchlist.service';
import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-movie-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movie-card.component.html',
  styleUrl: './movie-card.component.scss'
})
export class MovieCardComponent {
  @Input() movie!: Movie;
  inWatchlist = false;
  watchlistLoading = false;
  watchlistUpdating = false;

  constructor(
    private router: Router,
    private watchlistService: WatchlistService,
    private toastService: ToastService,
    private authService: AuthService
  ) {
    if (this.authService.isLoggedIn() && this.movie) {
      this.checkWatchlistStatus();
    }
  }

  ngOnChanges() {
    if (this.authService.isLoggedIn() && this.movie) {
      this.checkWatchlistStatus();
    }
  }

  checkWatchlistStatus() {
    if (!this.authService.isLoggedIn() || !this.movie) return;

    this.watchlistLoading = true;
    this.watchlistService.checkMovieInWatchlist(this.movie.id).subscribe({
      next: (inList) => {
        this.inWatchlist = inList;
        this.watchlistLoading = false;
      },
      error: (err) => {
        console.error('Error checking watchlist status:', err);
        this.watchlistLoading = false;
      }
    });
  }

  onMovieClick() {
    this.router.navigate(['/movies', this.movie.id]);
  }

  toggleWatchlist(event: Event) {
    event.stopPropagation();

    if (!this.authService.isLoggedIn()) {
      this.toastService.error('Please log in to use the watchlist');
      return;
    }

    if (this.inWatchlist) {
      this.removeFromWatchlist();
    } else {
      this.addToWatchlist();
    }
  }

  addToWatchlist() {
    this.watchlistUpdating = true;
    this.watchlistService.addToWatchlist({
      movieId: this.movie.id,
      movieTitle: this.movie.title
    }).subscribe({
      next: () => {
        this.inWatchlist = true;
        this.watchlistUpdating = false;
        this.toastService.success('Added to watchlist!');
      },
      error: (err) => {
        this.toastService.error('Failed to add to watchlist. Please try again.');
        console.error('Error adding to watchlist:', err);
        this.watchlistUpdating = false;
      }
    });
  }

  removeFromWatchlist() {
    this.watchlistUpdating = true;
    this.watchlistService.removeFromWatchlist(this.movie.id).subscribe({
      next: () => {
        this.inWatchlist = false;
        this.watchlistUpdating = false;
        this.toastService.success('Removed from watchlist!');
      },
      error: (err) => {
        this.toastService.error('Failed to remove from watchlist. Please try again.');
        console.error('Error removing from watchlist:', err);
        this.watchlistUpdating = false;
      }
    });
  }

  getDirectorsCount(): number {
    if (!this.movie.directors) return 0;
    return this.movie.directors.length;
  }

  getActorsCount(): number {
    if (!this.movie.actors) return 0;
    return this.movie.actors.length;
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}
