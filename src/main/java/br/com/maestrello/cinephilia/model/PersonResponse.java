package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {

  private Long page;
  private List<Person> results;

  @JsonProperty("total_pages")
  private Long totalPages;

  @JsonProperty("total_results")
  private Long totalResults;
}
