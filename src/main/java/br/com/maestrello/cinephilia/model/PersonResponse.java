package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class PersonResponse {

  private Long page;
  private List<Person> results;

  @JsonProperty("total_pages")
  private Long totalPages;

  @JsonProperty("total_results")
  private Long totalResults;
}
