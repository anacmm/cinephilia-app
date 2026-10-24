package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.TvSeriesApi;
import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.ProductionCredits;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TvService {
  private final TvSeriesApi tvSeriesApi;

  public List<Cast> getCredits(Long tvId) {
    return tvSeriesApi.getCredits(tvId).getCast();
  }

  public ProductionCredits getCreditsDetails(Long tvId) {
    return ProductionCredits.from(tvSeriesApi.getCredits(tvId));
  }
}
