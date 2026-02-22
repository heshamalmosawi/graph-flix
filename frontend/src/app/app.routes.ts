import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth';
import { TwoFactorVerifyComponent } from './auth/two-factor-verify/two-factor-verify';
import { SecuritySettingsComponent } from './settings/security-settings';
import { HomeComponent } from './home/home.component';
import { MovieSearchPage } from './movies/movie-search/movie-search.component';
import { MovieDetailComponent } from './movies/movie-detail/movie-detail.component';
import { WatchlistComponent } from './watchlist/watchlist.component';
import { authGuard } from './guards/auth.guard';
import { twoFactorGuard } from './guards/two-factor.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth', component: AuthComponent },
  { path: 'auth/2fa/verify', component: TwoFactorVerifyComponent, canActivate: [twoFactorGuard] },
  { path: 'settings/security', component: SecuritySettingsComponent, canActivate: [authGuard] },
  { path: 'movies/search', component: MovieSearchPage },
  { path: 'movies/:id', component: MovieDetailComponent },
  { path: 'watchlist', component: WatchlistComponent, canActivate: [authGuard] }
];
