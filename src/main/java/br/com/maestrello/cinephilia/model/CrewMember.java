package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Relevant fields from a TMDB credits crew entry. */
public record CrewMember(
    Long id, String name, @JsonProperty("profile_path") String profilePath, String job) {}
