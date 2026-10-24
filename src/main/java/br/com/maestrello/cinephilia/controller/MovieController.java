package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.Movie;
import br.com.maestrello.cinephilia.model.ProductionCredits;
import br.com.maestrello.cinephilia.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("movies")
@RequiredArgsConstructor
public class MovieController {

  private final MovieService movieService;

  @Operation(
      summary = "Get popular movies",
      description = "Returns a list of popular movies from TMDB.")
  @CinephiliaApiResponses
  @GetMapping("/popular")
  public ResponseEntity<List<Movie>> getPopularMovies() {
    return ResponseEntity.ok().body(movieService.getPopularMovies());
  }

  @Operation(
      summary = "Get top rated movies",
      description = "Returns a list of top-rated movies from TMDB.")
  @CinephiliaApiResponses
  @GetMapping("/top_rated")
  public ResponseEntity<List<Movie>> getTopRatedMovies() {
    return ResponseEntity.ok().body(movieService.getTopRatedMovies());
  }

  @Operation(summary = "Get a movie by ID")
  @CinephiliaApiResponses
  @GetMapping("/{movie_id}")
  public ResponseEntity<Movie> getMovieDetails(@PathVariable("movie_id") Long movieId) {
    return ResponseEntity.ok(movieService.getMovieDetails(movieId));
  }

  @Operation(
      summary = "Get movie cast",
      description = "Returns cast members from TMDB, in billing order.")
  @CinephiliaApiResponses
  @GetMapping("/{movieId}/credits")
  public ResponseEntity<List<Cast>> getCredits(@PathVariable("movieId") Long movieId) {
    return ResponseEntity.ok(movieService.getCredits(movieId));
  }

  @Operation(
      summary = "Get movie cast and directors",
      description =
          "Returns unchanged cast and crew whose job is Director. The cast-only /credits endpoint is unchanged.")
  @CinephiliaApiResponses
  @GetMapping("/{movieId}/credits/details")
  public ResponseEntity<ProductionCredits> getCreditsDetails(
      @PathVariable("movieId") Long movieId) {
    return ResponseEntity.ok(movieService.getCreditsDetails(movieId));
  }
}
