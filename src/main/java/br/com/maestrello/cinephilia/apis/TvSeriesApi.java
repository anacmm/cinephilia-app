package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.model.CreditResponse;
import br.com.maestrello.cinephilia.model.TvShow;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tv-api", url = "${tmdb.base-url}")
public interface TvSeriesApi {

  @GetMapping("/tv/popular")
  public abstract List<TvShow> getPopularTvShows();

  @GetMapping("/tv/top_rated")
  public abstract List<TvShow> getTopRatedTvShows();

  @GetMapping("/tv/{tvId}/credits")
  CreditResponse getCredits(@PathVariable("tvId") Long tvId);
}
