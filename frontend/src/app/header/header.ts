import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';
import { WatchlistService } from '../services/watchlist.service';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header implements OnInit {
  isLoggedIn$: Observable<boolean>;
  watchlistCount$: Observable<number>;
  private refreshWatchlist$ = new BehaviorSubject<void>(undefined);

  constructor(
    private authService: AuthService,
    private watchlistService: WatchlistService,
    private router: Router
  ) {
    this.isLoggedIn$ = this.authService.user$.pipe(map(user => !!user));
    this.watchlistCount$ = this.refreshWatchlist$.pipe(
      switchMap(() => this.isLoggedIn$),
      switchMap(isLoggedIn => {
        if (isLoggedIn) {
          return this.watchlistService.getWatchlistCount().pipe(
            catchError(() => of(0))
          );
        }
        return of(0);
      })
    );
  }

  ngOnInit() {
    this.refreshWatchlist$.next();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth']);
    this.refreshWatchlist$.next();
  }
}
