package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movie {

  private Boolean adult;

  @JsonProperty("backdrop_path")
  private String backdropPath;

  @JsonProperty("genre_ids")
  private List<Long> genreIds;

  private List<Genre> genres;
  private Long id;

  @JsonProperty("original_language")
  private String originalLanguage;

  @JsonProperty("original_title")
  private String originalTitle;

  private String overview;

  private Double popularity;

  @JsonProperty("poster_path")
  private String posterPath;

  @JsonProperty("release_date")
  private String releaseDate;

  private String title;

  private Boolean video;

  @JsonProperty("vote_average")
  private Double voteAverage;

  @JsonProperty("vote_count")
  private Long voteCount;
}
