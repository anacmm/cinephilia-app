package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cast {

  @JsonProperty("poster_path")
  String posterPath;

  private Boolean adult;
  private String character;
  private Long gender;
  private Long id;

  @JsonProperty("known_for_department")
  private String knownForDepartment;

  @JsonAlias("title")
  private String name;

  private Long order;

  @JsonProperty("original_name")
  private String originalName;

  @JsonProperty("first_air_date")
  private String firstAirDate;

  @JsonProperty("release_date")
  private String releaseDate;

  private Double popularity;

  @JsonProperty("profile_path")
  private String profilePath;
}
