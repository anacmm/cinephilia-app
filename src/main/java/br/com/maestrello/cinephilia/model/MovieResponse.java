package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class MovieResponse {

  private Long page;
  private List<Movie> results;

  @JsonProperty("total_pages")
  private Long totalPages;

  @JsonProperty("total_results")
  private Long totalResults;
}
