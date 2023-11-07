package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.domain.Movie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(name = "movie-api", url = "${api.base-url}")
public interface MovieApi {

    @GetMapping("/movie/popular")
    List<Movie> getPopularMovies();

    @GetMapping("/movie/top_rated")
    List<Movie> getTopRatedMovies();

}