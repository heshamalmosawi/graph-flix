import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginResponse {
  name: string;
  token: string;
  expiresAt: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiBaseUrl}/users/auth`;

  private userSubject = new BehaviorSubject<LoginResponse | null>(null);
  user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.userSubject.next(JSON.parse(storedUser));
    }
  }

  login(credentials: any): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        this.userSubject.next(response);
        localStorage.setItem('user', JSON.stringify(response));
        localStorage.setItem('token', response.token);
      })
    );
  }

  register(data: any): Observable<string> {
    // The backend returns a plain string message, so we must set responseType: 'text'
    return this.http.post(`${this.API_URL}/register`, data, { responseType: 'text' });
  }

  logout() {
    this.userSubject.next(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }
}
