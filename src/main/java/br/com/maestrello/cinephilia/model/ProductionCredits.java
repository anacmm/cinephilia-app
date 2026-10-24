package br.com.maestrello.cinephilia.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProductionCredits(
    @Schema(description = "Unchanged cast entries in TMDB order") List<Cast> cast,
    @Schema(
            description =
                "Crew entries whose job is exactly Director; may be empty. For TV, latest-season credits only, not overall series directors.")
        List<Director> directors) {

  public static ProductionCredits from(CreditResponse response) {
    List<Director> directors =
        response.getCrew() == null
            ? List.of()
            : response.getCrew().stream()
                .filter(member -> "Director".equals(member.job()))
                .map(member -> new Director(member.id(), member.name(), member.profilePath()))
                .toList();
    return new ProductionCredits(response.getCast(), directors);
  }
}
