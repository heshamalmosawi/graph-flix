export interface RecommendedMovie {
  id: string;
  title: string;
  released?: number;
  tagline?: string;
  reason: string;
  score: number;
}

export interface RecommendationResponse {
  movies: RecommendedMovie[];
}
