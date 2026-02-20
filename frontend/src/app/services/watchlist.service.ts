import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

import {
  Watchlist,
  AddToWatchlistRequest,
  PagedWatchlistResponse
} from '../models/watchlist.model';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {
  private readonly API_URL = `${environment.apiBaseUrl}/watchlist`;

  constructor(private http: HttpClient) {}

  addToWatchlist(request: AddToWatchlistRequest): Observable<Watchlist> {
    return this.http.post<Watchlist>(this.API_URL, request);
  }

  removeFromWatchlist(movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${movieId}`);
  }

  getUserWatchlist(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'addedAt'
  ): Observable<PagedWatchlistResponse> {
    return this.http.get<PagedWatchlistResponse>(this.API_URL, {
      params: { page, size, sortBy }
    });
  }

  checkMovieInWatchlist(movieId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/check/${movieId}`);
  }

  getWatchlistCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/count`);
  }
}
