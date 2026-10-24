package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record Director(
    @Schema(description = "TMDB person ID") Long id,
    String name,
    @JsonProperty("profile_path") @Schema(description = "TMDB profile image path; may be null")
        String profilePath) {}
