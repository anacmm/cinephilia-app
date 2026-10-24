package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.model.CreditResponse;
import br.com.maestrello.cinephilia.model.Movie;
import br.com.maestrello.cinephilia.model.MovieResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-api", url = "${tmdb.base-url}")
public interface MovieApi {

  @GetMapping("/movie/popular")
  MovieResponse getPopularMovies();

  @GetMapping("/movie/top_rated")
  MovieResponse getTopRatedMovies();

  @GetMapping("/movie/{movie_id}")
  Movie getMovie(@PathVariable("movie_id") Long movieId);

  @GetMapping("/movie/{movieId}/credits")
  CreditResponse getCredits(@PathVariable("movieId") Long movieId);
}
