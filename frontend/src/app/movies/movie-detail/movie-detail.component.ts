import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { MovieService } from '../../services/movie.service';
import { RatingService } from '../../services/rating.service';
import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../../auth/auth.service';
import { Movie, Person } from '../../models/movie.model';
import { AverageRatingDTO, CreateRatingRequest, Rating } from '../../models/rating.model';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movie-detail.component.html',
  styleUrl: './movie-detail.component.scss'
})
export class MovieDetailComponent implements OnInit {
  movie: Movie | null = null;
  loading = false;
  error: string | null = null;
  averageRating: AverageRatingDTO | null = null;
  ratingLoading = false;

  userExistingRating: Rating | null = null;
  userRatingLoading = false;

  showRatingForm = false;
  isEditMode = false;
  userRating: CreateRatingRequest = {
    movieId: '',
    rating: 5,
    comment: ''
  };
  ratingSubmitting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private movieService: MovieService,
    private ratingService: RatingService,
    private toastService: ToastService,
    private authService: AuthService
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
    this.userRating.movieId = movieId;

    this.movieService.getMovieById(movieId).subscribe({
      next: (movie) => {
        this.movie = movie;
        this.loading = false;
        this.loadAverageRating(movieId);
        if (this.isLoggedIn()) {
          this.loadUserRating(movieId);
        }
      },
      error: (err) => {
        this.error = 'Failed to load movie details. Please try again later.';
        this.loading = false;
        console.error('Error loading movie:', err);
      }
    });
  }

  loadAverageRating(movieId: string) {
    this.ratingLoading = true;
    this.ratingService.getAverageRating(movieId).subscribe({
      next: (avgRating) => {
        this.averageRating = avgRating;
        this.ratingLoading = false;
      },
      error: (err) => {
        console.error('Error loading average rating:', err);
        this.ratingLoading = false;
      }
    });
  }

  loadUserRating(movieId: string) {
    const token = localStorage.getItem('token');
    if (!token) return;

    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.uuid || user.id || user.name || user.email;
    
    console.log('MovieDetailComponent: loadUserRating - Using userId:', userId);

    if (!userId) return;

    this.userRatingLoading = true;
    this.ratingService.getUserRatings(userId, 0, 100).subscribe({
      next: (response) => {
        const movieRating = response.content.find(r => r.movieId === movieId);
        if (movieRating) {
          this.userExistingRating = movieRating;
        } else {
          this.userExistingRating = null;
        }
        this.userRatingLoading = false;
      },
      error: (err) => {
        console.error('Error loading user rating:', err);
        this.userRatingLoading = false;
      }
    });
  }

  goBack() {
    this.router.navigate(['/movies']);
  }

  rateMovie() {
    if (!this.movie) return;
    if (this.userExistingRating) {
      this.isEditMode = true;
      this.userRating.rating = this.userExistingRating.rating;
      this.userRating.comment = this.userExistingRating.comment;
    } else {
      this.isEditMode = false;
      this.userRating.rating = 5;
      this.userRating.comment = '';
    }
    this.showRatingForm = true;
  }

  cancelRating() {
    this.showRatingForm = false;
    this.isEditMode = false;
    this.userRating.rating = 5;
    this.userRating.comment = '';
  }

  submitRating() {
    if (!this.userRating.rating || this.userRating.rating < 1 || this.userRating.rating > 10) {
      this.toastService.error('Please select a rating between 1 and 10');
      return;
    }

    this.ratingSubmitting = true;

    this.ratingService.createRating(this.userRating).subscribe({
      next: (response) => {
        this.toastService.success(this.isEditMode ? 'Rating updated successfully!' : 'Rating submitted successfully!');
        this.userExistingRating = response;
        this.showRatingForm = false;
        this.isEditMode = false;
        this.userRating.rating = 5;
        this.userRating.comment = '';
        this.loadAverageRating(this.userRating.movieId);
        this.ratingSubmitting = false;
      },
      error: (err) => {
        this.toastService.error('Failed to submit rating. Please try again.');
        console.error('Error submitting rating:', err);
        this.ratingSubmitting = false;
      }
    });
  }

  deleteRating() {
    if (!this.userExistingRating || !confirm('Are you sure you want to delete your rating?')) {
      return;
    }

    this.ratingService.deleteRating(this.userExistingRating.id).subscribe({
      next: () => {
        this.toastService.success('Rating deleted successfully!');
        this.userExistingRating = null;
        this.userRating.rating = 5;
        this.userRating.comment = '';
        this.loadAverageRating(this.userRating.movieId);
      },
      error: (err) => {
        this.toastService.error('Failed to delete rating. Please try again.');
        console.error('Error deleting rating:', err);
      }
    });
  }

  getStars(rating: number): number[] {
    const stars = [];
    for (let i = 1; i <= 10; i++) {
      stars.push(i);
    }
    return stars;
  }

  getRatingColor(rating: number): string {
    if (rating >= 8) return '#10b981';
    if (rating >= 6) return '#3b82f6';
    if (rating >= 4) return '#f59e0b';
    return '#ef4444';
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

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
