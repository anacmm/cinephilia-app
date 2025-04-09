package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class TvShow {

  private Boolean adult;

  @JsonProperty("backdrop_path")
  private String backdropPath;

  @JsonProperty("episode_run_time")
  private List<Long> episodeRunTime;

  @JsonProperty("first_air_date")
  private String firstAirDate;

  private List<Genre> genres;
  private String homepage;

  private Long id;

  @JsonProperty("in_production")
  private Boolean inProduction;

  private List<String> languages;

  @JsonProperty("last_air_date")
  private String lastAirDate;

  private String name;

  @JsonProperty("next_episode_to_air")
  private Object nextEpisodeToAir;

  @JsonProperty("number_of_episodes")
  private Long numberOfEpisodes;

  @JsonProperty("number_of_seasons")
  private Long numberOfSeasons;

  @JsonProperty("origin_country")
  private List<String> originCountry;

  @JsonProperty("original_language")
  private String originalLanguage;

  @JsonProperty("original_name")
  private String originalName;

  private String overview;

  private Double popularity;

  @JsonProperty("poster_path")
  private String posterPath;

  private String status;

  private String tagline;

  private String type;

  @JsonProperty("vote_average")
  private Double voteAverage;

  @JsonProperty("vote_count")
  private Long voteCount;
}
