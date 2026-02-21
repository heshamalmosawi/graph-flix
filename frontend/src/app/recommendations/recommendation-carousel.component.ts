import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { RecommendationService } from '../services/recommendation.service';
import { AuthService, LoginResponse } from '../auth/auth.service';
import { RecommendedMovie } from '../models/recommendation.model';

@Component({
  selector: 'app-recommendation-carousel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recommendation-carousel.component.html',
  styleUrl: './recommendation-carousel.component.scss'
})
export class RecommendationCarouselComponent implements OnInit {
  @Input() limit: number = 10;

  recommendations: RecommendedMovie[] = [];
  loading = false;
  error: string | null = null;
  currentUser: LoginResponse | null = null;

  carouselIndex = 0;
  circular = true;
  readonly cardWidth = 320;
  readonly cardGap = 16;
  readonly mobileCardWidth = 280;
  isMobile = false;

  get canScrollLeft(): boolean {
    if (this.circular) {
      return this.recommendations.length > 1;
    }
    return this.carouselIndex > 0;
  }

  get canScrollRight(): boolean {
    if (this.circular) {
      return this.recommendations.length > 1;
    }
    return this.carouselIndex < this.recommendations.length - 1;
  }

  constructor(
    private recommendationService: RecommendationService,
    private authService: AuthService,
    private router: Router
  ) {
    this.checkMobile();
    window.addEventListener('resize', () => this.checkMobile());
  }

  ngOnInit() {
    this.authService.user$.pipe(takeUntilDestroyed()).subscribe(user => {
      this.currentUser = user;
      this.loadRecommendations();
    });
  }

  get slideWidth(): number {
    return this.isMobile ? this.mobileCardWidth : this.cardWidth;
  }

  private checkMobile(): void {
    this.isMobile = window.innerWidth <= 768;
  }

  loadRecommendations() {
    this.loading = true;
    this.error = null;

    if (this.currentUser) {
      this.recommendationService.getPersonalizedRecommendations(this.limit).subscribe({
        next: (response) => {
          this.recommendations = response.movies;
          this.loading = false;
        },
        error: (err) => {
          this.loadTrendingRecommendations();
        }
      });
    } else {
      this.loadTrendingRecommendations();
    }
  }

  private loadTrendingRecommendations() {
    this.recommendationService.getTrendingRecommendations(this.limit).subscribe({
      next: (response) => {
        this.recommendations = response.movies;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load recommendations.';
        this.loading = false;
        console.error('Error loading recommendations:', err);
      }
    });
  }

  scrollLeft() {
    if (this.circular && this.recommendations.length > 1) {
      this.carouselIndex = this.carouselIndex === 0 
        ? this.recommendations.length - 1 
        : this.carouselIndex - 1;
    } else if (this.canScrollLeft) {
      this.carouselIndex = Math.max(0, this.carouselIndex - 1);
    }
  }

  scrollRight() {
    if (this.circular && this.recommendations.length > 1) {
      this.carouselIndex = this.carouselIndex === this.recommendations.length - 1 
        ? 0 
        : this.carouselIndex + 1;
    } else if (this.canScrollRight) {
      this.carouselIndex = Math.min(this.recommendations.length - 1, this.carouselIndex + 1);
    }
  }

  goToMovie(movieId: string) {
    this.router.navigate(['/movies', movieId]);
  }

  goToSlide(index: number) {
    this.carouselIndex = index;
  }
}
