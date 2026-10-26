package br.com.maestrello.cinephilia.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GameSelection(@NotNull GameSelectionType type, @NotNull @Positive Long id) {}
