import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth';
import { HomeComponent } from './home/home.component';
import { MovieSearchPage } from './movies/movie-search/movie-search.component';
import { MovieDetailComponent } from './movies/movie-detail/movie-detail.component';
import { WatchlistComponent } from './watchlist/watchlist.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth', component: AuthComponent },
  { path: 'movies/search', component: MovieSearchPage },
  { path: 'movies/:id', component: MovieDetailComponent },
  { path: 'watchlist', component: WatchlistComponent }
];
