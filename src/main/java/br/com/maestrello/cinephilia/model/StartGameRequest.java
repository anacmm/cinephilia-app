package br.com.maestrello.cinephilia.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StartGameRequest(
    @NotBlank @Size(max = 50) String playerName,
    @NotNull @Positive Long startPersonId,
    @NotNull @Positive Long targetPersonId) {}
