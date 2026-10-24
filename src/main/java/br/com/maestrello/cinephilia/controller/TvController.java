package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.ProductionCredits;
import br.com.maestrello.cinephilia.service.TvService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tv")
@RequiredArgsConstructor
public class TvController {
  private final TvService tvService;

  @Operation(
      summary = "Get TV show cast",
      description = "Returns latest-season cast members from TMDB, in billing order.")
  @CinephiliaApiResponses
  @GetMapping("/{tvId}/credits")
  public ResponseEntity<List<Cast>> getCredits(@PathVariable("tvId") Long tvId) {
    return ResponseEntity.ok(tvService.getCredits(tvId));
  }

  @Operation(
      summary = "Get tv cast and directors",
      description =
          "Returns unchanged latest-season cast and crew whose job is Director. May contain multiple directors or none; not an all-season view or overall series directors. The cast-only /credits endpoint is unchanged.")
  @CinephiliaApiResponses
  @GetMapping("/{tvId}/credits/details")
  public ResponseEntity<ProductionCredits> getCreditsDetails(@PathVariable("tvId") Long tvId) {
    return ResponseEntity.ok(tvService.getCreditsDetails(tvId));
  }
}
