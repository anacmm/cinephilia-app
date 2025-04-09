package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.Movie;
import br.com.maestrello.cinephilia.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/popular")
    public ResponseEntity<List<Movie>> getPopularMovies() {
        return ResponseEntity.ok().body(movieService.getPopularMovies());
    }

    @GetMapping("/top_rated")
    public ResponseEntity<List<Movie>> getTopRatedMovies() {
        return ResponseEntity.ok().body(movieService.getTopRatedMovies());
    }

    @GetMapping("/{movie_id}")
    public Movie getMovieDetails(@PathVariable("movie_id") Long movieId) {
        return movieService.getMovieDetails(movieId);
    }

}