export interface Movie {
  id: string;
  title: string;
  released?: number;
  tagline?: string;
  actors?: Person[] | string[];
  directors?: Person[] | string[];
}

export interface Person {
  id: string;
  name: string;
  born?: number;
}

export interface MovieSearchRequest {
  title?: string;
  person?: string;
}

export interface PagedMovieResponse {
  content: Movie[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
