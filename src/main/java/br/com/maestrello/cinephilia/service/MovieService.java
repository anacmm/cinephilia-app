package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.MovieApi;
import br.com.maestrello.cinephilia.model.Movie;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

  private final MovieApi movieApi;

  @Autowired
  public MovieService(MovieApi movieApi) {
    this.movieApi = movieApi;
  }

  public List<Movie> getPopularMovies() {
    return movieApi.getPopularMovies().getResults();
  }

  public List<Movie> getTopRatedMovies() {
    return movieApi.getTopRatedMovies().getResults();
  }

  public Movie getMovieDetails(Long movieId) {
    return movieApi.getMovie(movieId);
  }
}
