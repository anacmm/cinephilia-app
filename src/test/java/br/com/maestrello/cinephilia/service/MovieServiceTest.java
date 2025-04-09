package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.MovieApi;
import br.com.maestrello.cinephilia.model.Movie;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieApi movieApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPopularMovies_ShouldReturnMoviesList() throws Exception {
        // Arrange
//        MovieApi.MovieResponse mockResponse = new MovieApi.MovieResponse();
//        List<Movie> movies = Arrays.asList(
//                new Movie(1L, "Popular Movie 1"),
//                new Movie(2L, "Popular Movie 2")
//        );
//        mockResponse.setResults(movies);
//        when(movieApi.getPopularMovies()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/movies/popular")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Popular Movie 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Popular Movie 2"));
    }

    @Test
    void getTopRatedMovies_ShouldReturnMoviesList() throws Exception {
        // Arrange
//        MovieApi.MovieResponse mockResponse = new MovieApi.MovieResponse();
//        List<Movie> movies = Arrays.asList(
//                new Movie(1L, "Top Rated Movie 1"),
//                new Movie(2L, "Top Rated Movie 2")
//        );
//        mockResponse.setResults(movies);
//        when(movieApi.getTopRatedMovies()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/movies/top-rated")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Top Rated Movie 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Top Rated Movie 2"));
    }

    @Test
    void getMovieDetails_ShouldReturnMovie() throws Exception {
        // Arrange
        Long movieId = 1L;
//        Movie movie = new Movie(movieId, "Test Movie");
//        when(movieApi.getMovie(movieId)).thenReturn(movie);

        // Act & Assert
        mockMvc.perform(get("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(movieId))
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }
}
