package br.com.maestrello.cinephilia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import br.com.maestrello.cinephilia.apis.MovieApi;
import br.com.maestrello.cinephilia.model.Movie;
import br.com.maestrello.cinephilia.testutil.DataHelper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MovieServiceTest {

  @Mock private MovieApi movieApi;

  @InjectMocks private MovieService movieService;

  @Test
  void getPopularMovies_shouldReturnMovieList() {
    when(movieApi.getPopularMovies()).thenReturn(DataHelper.movieResponse());

    List<Movie> result = movieService.getPopularMovies();

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void getTopRatedMovies_shouldReturnMovieList() {
    when(movieApi.getTopRatedMovies()).thenReturn(DataHelper.movieResponse());

    List<Movie> result = movieService.getTopRatedMovies();

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void getMovieDetails_shouldReturnMovie() {
    when(movieApi.getMovie(1L)).thenReturn(DataHelper.movie());

    Movie result = movieService.getMovieDetails(1L);

    assertEquals(DataHelper.movie(), result);
  }
}
