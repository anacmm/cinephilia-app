package br.com.maestrello.cinephilia.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CompleteGameRequest(
    @NotNull @Size(min = 3) List<@NotNull @Valid GameSelection> path) {}
