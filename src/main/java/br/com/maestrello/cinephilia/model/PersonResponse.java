package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonResponse {

  private Long page;
  private List<Person> results;

  @JsonProperty("total_pages")
  private Long totalPages;

  @JsonProperty("total_results")
  private Long totalResults;
}
