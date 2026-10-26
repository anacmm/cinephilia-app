package br.com.maestrello.cinephilia.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GameSelectionType {
  PERSON,
  MOVIE,
  TV;

  @JsonCreator
  public static GameSelectionType fromJson(String value) {
    return valueOf(value);
  }
}
