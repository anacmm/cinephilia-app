package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.domain.Movie;
import br.com.maestrello.cinephilia.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies/popular")
    public List<Movie> getPopularMovies() {
        return movieService.getPopularMovies();
    }

}