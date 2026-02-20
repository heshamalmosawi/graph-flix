export interface Rating {
  id: number;
  rating: number;
  comment: string;
  timestamp: string;
  userId: string;
  userName: string;
  movieId: string;
  movieTitle: string;
}

export interface CreateRatingRequest {
  movieId: string;
  rating: number;
  comment: string;
}

export interface AverageRatingDTO {
  movieId: string;
  average: number;
  count: number;
}

export interface PagedRatingResponse {
  content: Rating[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}
