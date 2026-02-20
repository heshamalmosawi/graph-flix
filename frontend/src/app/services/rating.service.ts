import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

import {
  Rating,
  CreateRatingRequest,
  UpdateRatingRequest,
  AverageRatingDTO,
  PagedRatingResponse
} from '../models/rating.model';

@Injectable({
  providedIn: 'root'
})
export class RatingService {
  private readonly API_URL = `${environment.apiBaseUrl}/ratings`;

  constructor(private http: HttpClient) {}

  createRating(request: CreateRatingRequest): Observable<Rating> {
    return this.http.post<Rating>(this.API_URL, request);
  }

  updateRating(id: number, request: UpdateRatingRequest): Observable<Rating> {
    return this.http.put<Rating>(`${this.API_URL}/${id}`, request);
  }

  deleteRating(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getUserRatings(
    userId: string,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'timestamp'
  ): Observable<PagedRatingResponse> {
    return this.http.get<PagedRatingResponse>(`${this.API_URL}/user/${userId}`, {
      params: { page, size, sortBy }
    });
  }

  getMovieRatings(
    movieId: string,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'rating'
  ): Observable<PagedRatingResponse> {
    return this.http.get<PagedRatingResponse>(`${this.API_URL}/movie/${movieId}`, {
      params: { page, size, sortBy }
    });
  }

  getAverageRating(movieId: string): Observable<AverageRatingDTO> {
    return this.http.get<AverageRatingDTO>(
      `${this.API_URL}/movie/${movieId}/average`
    );
  }
}
