export interface Watchlist {
  id: number;
  userId: string;
  movieId: string;
  movieTitle: string;
  addedAt: string;
}

export interface AddToWatchlistRequest {
  movieId: string;
  movieTitle: string;
}

export interface PagedWatchlistResponse {
  content: Watchlist[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  first: boolean;
  last: boolean;
}
