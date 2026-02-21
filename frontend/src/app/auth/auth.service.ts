import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginResponse {
  name: string;
  token: string;
  expiresAt: number;
  status?: '2FA_REQUIRED' | 'SUCCESS' | null;
  message?: string;
  id?: string;
}

export interface TwoFactorSetupResponse {
  qrCode: string;
  secret: string;
}

export interface TwoFactorRequest {
  code: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiBaseUrl}/users/auth`;

  private userSubject = new BehaviorSubject<LoginResponse | null>(null);
  user$ = this.userSubject.asObservable();
  
  private temporaryToken: string | null = null;

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.userSubject.next(JSON.parse(storedUser));
    }
  }

  login(credentials: any): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        if (response.status === '2FA_REQUIRED') {
          this.temporaryToken = response.token;
          localStorage.setItem('temporaryToken', response.token);
        } else {
          this.userSubject.next(response);
          localStorage.setItem('user', JSON.stringify(response));
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  register(data: any): Observable<string> {
    // The backend returns a plain string message, so we must set responseType: 'text'
    return this.http.post(`${this.API_URL}/register`, data, { responseType: 'text' });
  }

  logout() {
    this.userSubject.next(null);
    this.temporaryToken = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('temporaryToken');
  }

  isLoggedIn(): boolean {
    return this.userSubject.value !== null;
  }

  is2FARequired(): boolean {
    return this.temporaryToken !== null;
  }

  getTemporaryToken(): string | null {
    return this.temporaryToken;
  }

  clearTemporaryToken() {
    this.temporaryToken = null;
    localStorage.removeItem('temporaryToken');
  }

  verify2fa(code: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/verify-2fa`, { code }).pipe(
      tap(response => {
        this.clearTemporaryToken();
        this.userSubject.next(response);
        localStorage.setItem('user', JSON.stringify(response));
        localStorage.setItem('token', response.token);
      })
    );
  }

  setup2fa(): Observable<TwoFactorSetupResponse> {
    return this.http.post<TwoFactorSetupResponse>(`${this.API_URL}/2fa/setup`, {});
  }

  enable2fa(code: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/2fa/enable`, { code });
  }

  disable2fa(code: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/2fa/disable`, { code });
  }
}
