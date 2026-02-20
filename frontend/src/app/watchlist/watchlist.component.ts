import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { WatchlistService } from '../services/watchlist.service';
import { Watchlist, PagedWatchlistResponse } from '../models/watchlist.model';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './watchlist.component.html',
  styleUrl: './watchlist.component.scss'
})
export class WatchlistComponent implements OnInit {
  watchlist: Watchlist[] = [];
  loading = false;
  error: string | null = null;
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = false;

  constructor(
    private watchlistService: WatchlistService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadWatchlist();
  }

  loadWatchlist(page: number = 0) {
    if (!this.authService.isLoggedIn()) {
      this.error = 'Please log in to view your watchlist';
      return;
    }

    this.loading = true;
    this.error = null;
    this.currentPage = page;

    this.watchlistService.getUserWatchlist(page, this.pageSize).subscribe({
      next: (response: PagedWatchlistResponse) => {
        this.watchlist = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.first = response.first;
        this.last = response.last;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load watchlist. Please try again later.';
        this.loading = false;
        console.error('Error loading watchlist:', err);
      }
    });
  }

  nextPage() {
    if (!this.last) {
      this.loadWatchlist(this.currentPage + 1);
    }
  }

  previousPage() {
    if (!this.first) {
      this.loadWatchlist(this.currentPage - 1);
    }
  }

  goToPage(page: number) {
    this.loadWatchlist(page);
  }

  removeFromWatchlist(movieId: string, movieTitle: string) {
    if (!confirm(`Remove "${movieTitle}" from your watchlist?`)) {
      return;
    }

    this.watchlistService.removeFromWatchlist(movieId).subscribe({
      next: () => {
        this.watchlist = this.watchlist.filter(w => w.movieId !== movieId);
        this.totalElements--;
        if (this.watchlist.length === 0 && !this.first) {
          this.loadWatchlist(this.currentPage - 1);
        } else if (this.watchlist.length === 0) {
          this.loadWatchlist(this.currentPage);
        }
      },
      error: (err: any) => {
        console.error('Error removing from watchlist:', err);
      }
    });
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  getPages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages - 1, start + maxVisible - 1);

    if (end - start < maxVisible - 1) {
      start = Math.max(0, end - maxVisible + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }
}
