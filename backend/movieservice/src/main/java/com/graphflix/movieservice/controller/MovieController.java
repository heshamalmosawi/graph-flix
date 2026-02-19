package com.graphflix.movieservice.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.movieservice.dto.MovieDTO;
import com.graphflix.movieservice.dto.PagedMovieResponse;
import com.graphflix.movieservice.dto.PersonDTO;
import com.graphflix.movieservice.model.Movie;
import com.graphflix.movieservice.model.Person;
import com.graphflix.movieservice.service.MovieService;

@RestController
@RequestMapping("/")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable String id) {
        return movieService.getMovieById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<PagedMovieResponse> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> moviePage = movieService.getAllMovies(pageable);

        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PagedMovieResponse response = PagedMovieResponse.builder()
                .content(movieDTOs)
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .number(moviePage.getNumber())
                .size(moviePage.getSize())
                .first(moviePage.isFirst())
                .last(moviePage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDTO>> getAllMoviesWithoutPagination() {
        List<Movie> movies = movieService.getAllMovies();
        List<MovieDTO> movieDTOs = movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movieDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedMovieResponse> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String person,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Movie> movies;

        if (title != null && !title.isEmpty()) {
            movies = movieService.searchByTitle(title);
        } else if (person != null && !person.isEmpty()) {
            movies = movieService.searchByPerson(person);
        } else {
            movies = movieService.getAllMovies();
        }

        List<MovieDTO> movieDTOs = movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        PagedMovieResponse response = PagedMovieResponse.builder()
                .content(movieDTOs)
                .totalElements(movies.size())
                .totalPages((int) Math.ceil((double) movies.size() / size))
                .number(page)
                .size(size)
                .first(page == 0)
                .last(page >= (int) Math.ceil((double) movies.size() / size) - 1)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/title/{title}")
    public ResponseEntity<List<MovieDTO>> searchByTitle(@PathVariable String title) {
        List<Movie> movies = movieService.searchByTitle(title);
        List<MovieDTO> movieDTOs = movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movieDTOs);
    }

    @GetMapping("/search/person/{person}")
    public ResponseEntity<List<MovieDTO>> searchByPerson(@PathVariable String person) {
        List<Movie> movies = movieService.searchByPerson(person);
        List<MovieDTO> movieDTOs = movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movieDTOs);
    }

    private MovieDTO toDTO(Movie movie) {
        Set<PersonDTO> actorDTOs = movie.getActors() != null
                ? movie.getActors().stream()
                        .map(this::personToDTO)
                        .collect(Collectors.toSet())
                : null;

        Set<PersonDTO> directorDTOs = movie.getDirectors() != null
                ? movie.getDirectors().stream()
                        .map(this::personToDTO)
                        .collect(Collectors.toSet())
                : null;

        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .released(movie.getReleased())
                .tagline(movie.getTagline())
                .actors(actorDTOs)
                .directors(directorDTOs)
                .build();
    }

    private PersonDTO personToDTO(Person person) {
        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .born(person.getBorn())
                .build();
    }
}
