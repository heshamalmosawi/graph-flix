import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

import { Movie, MovieSearchRequest, PagedMovieResponse } from '../models/movie.model';

@Injectable({
  providedIn: 'root'
})
export class MovieService {
  private readonly API_URL = `${environment.apiBaseUrl}/movies`;

  constructor(private http: HttpClient) {}

  getAllMovies(page: number = 0, size: number = 20, sortBy: string = 'title', sortDir: string = 'asc'): Observable<PagedMovieResponse> {
    return this.http.get<PagedMovieResponse>(this.API_URL, {
      params: {
        page,
        size,
        sortBy,
        sortDir
      }
    });
  }

  getMovieById(id: string): Observable<Movie> {
    return this.http.get<Movie>(`${this.API_URL}/${id}`);
  }

  searchMovies(searchRequest: MovieSearchRequest, page: number = 0, size: number = 20): Observable<PagedMovieResponse> {
    return this.http.get<PagedMovieResponse>(`${this.API_URL}/search`, {
      params: {
        ...searchRequest,
        page,
        size
      }
    });
  }

  searchByTitle(title: string): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.API_URL}/search/title/${encodeURIComponent(title)}`);
  }

  searchByPerson(person: string): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.API_URL}/search/person/${encodeURIComponent(person)}`);
  }
}
