package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Person {

  private Boolean adult;

  @JsonProperty("also_known_as")
  private List<String> alsoKnownAs;

  private String biography;

  private String birthday;

  private Object deathday;

  private Long gender;

  private String homepage;

  private Long id;

  @JsonProperty("imdb_id")
  private String imdbId;

  @JsonProperty("known_for_department")
  private String knownForDepartment;

  private String name;

  @JsonProperty("place_of_birth")
  private String placeOfBirth;

  private Double popularity;

  @JsonProperty("profile_path")
  private String profilePath;
}
