
package br.com.maestrello.cinephilia.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;


@Data
public class TvShow {

    private Boolean adult;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("episode_run_time")
    private List<Long> episodeRunTime;
    @SerializedName("first_air_date")
    private String firstAirDate;

    private List<Genre> genres;
    private String homepage;

    private Long id;
    @SerializedName("in_production")
    private Boolean inProduction;

    private List<String> languages;
    @SerializedName("last_air_date")
    private String lastAirDate;

    private String name;

    @SerializedName("next_episode_to_air")
    private Object nextEpisodeToAir;
    @SerializedName("number_of_episodes")
    private Long numberOfEpisodes;
    @SerializedName("number_of_seasons")
    private Long numberOfSeasons;
    @SerializedName("origin_country")
    private List<String> originCountry;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("original_name")
    private String originalName;

    private String overview;

    private Double popularity;
    @SerializedName("poster_path")
    private String posterPath;

    private String status;

    private String tagline;

    private String type;
    @SerializedName("vote_average")
    private Double voteAverage;
    @SerializedName("vote_count")
    private Long voteCount;

}
