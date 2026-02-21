import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RecommendationResponse } from '../models/recommendation.model';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private readonly API_URL = `${environment.apiBaseUrl}/recommendations`;

  constructor(private http: HttpClient) {}

  getPersonalizedRecommendations(limit: number = 10): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(`${this.API_URL}/personalized`, {
      params: {
        limit
      }
    });
  }

  getTrendingRecommendations(limit: number = 10): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(`${this.API_URL}/trending`, {
      params: {
        limit
      }
    });
  }
}
