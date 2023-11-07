package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.domain.TvShow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "tv-api", url = "${api.base-url}")
public abstract class TvSeriesApi {

    @GetMapping("/tv/popular")
    public abstract List<TvShow> getPopularTvShows();

    @GetMapping("/tv/top_rated")
    public abstract List<TvShow> getTopRatedTvShows();

}