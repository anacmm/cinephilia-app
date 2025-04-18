package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.model.TvShow;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "tv-api", url = "${tmdb.base-url}")
public interface TvSeriesApi {

  @GetMapping("/tv/popular")
  public abstract List<TvShow> getPopularTvShows();

  @GetMapping("/tv/top_rated")
  public abstract List<TvShow> getTopRatedTvShows();
}
