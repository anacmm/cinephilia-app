package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.MovieApi;
import br.com.maestrello.cinephilia.apis.PersonApi;
import br.com.maestrello.cinephilia.apis.TvSeriesApi;
import br.com.maestrello.cinephilia.model.*;
import feign.FeignException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GamePathValidator {
  private final PersonApi personApi;
  private final MovieApi movieApi;
  private final TvSeriesApi tvApi;

  public void validate(GameSession game, List<GameSelection> path) {
    if (path == null || path.size() < 3 || path.size() % 2 == 0) {
      throw invalid("Path must alternate PERSON and MOVIE/TV and end with a person");
    }
    for (int i = 0; i < path.size(); i++) {
      GameSelection selection = path.get(i);
      if (selection == null
          || selection.type() == null
          || selection.id() == null
          || selection.id() <= 0
          || (i % 2 == 0) != (selection.type() == GameSelectionType.PERSON)) {
        throw invalid(
            "Each selection needs a positive ID and an alternating PERSON or MOVIE/TV type");
      }
    }
    if (!path.getFirst().id().equals(game.getStartPersonId())
        || !path.getLast().id().equals(game.getTargetPersonId())) {
      throw invalid("Path must begin with START and explicitly select TARGET as its final step");
    }

    // Caches live for this validation only. A movie and TV show with the same ID remain distinct.
    Map<GameSelection, Set<Long>> productionsByPersonAndType = new HashMap<>();
    Map<GameSelection, Set<Long>> castByProduction = new HashMap<>();
    try {
      for (int i = 1; i < path.size(); i += 2) {
        GameSelection previous = path.get(i - 1);
        GameSelection production = path.get(i);
        GameSelection next = path.get(i + 1);
        var personKey = new GameSelection(production.type(), previous.id());
        Set<Long> credits =
            productionsByPersonAndType.computeIfAbsent(
                personKey,
                key ->
                    ids(
                        key.type() == GameSelectionType.MOVIE
                            ? personApi.getMovieCredits(key.id())
                            : personApi.getTvCredits(key.id())));
        if (!credits.contains(production.id())) {
          throw invalid("Person-to-production transition is not supported by TMDB credits");
        }
        Set<Long> cast =
            castByProduction.computeIfAbsent(
                production,
                key ->
                    ids(
                        key.type() == GameSelectionType.MOVIE
                            ? movieApi.getCredits(key.id())
                            : tvApi.getCredits(key.id())));
        if (!cast.contains(next.id())) {
          throw invalid("Production-to-person transition is not supported by TMDB cast");
        }
      }
    } catch (FeignException ex) {
      if (ex.status() == 404) {
        throw invalid("Path references a person or production not found in TMDB");
      }
      throw new ResponseStatusException(
          ex.status() < 0 || ex.status() == 429 || ex.status() >= 500
              ? HttpStatus.SERVICE_UNAVAILABLE
              : HttpStatus.BAD_GATEWAY,
          "TMDB is unavailable; game completion could not be validated");
    }
  }

  private Set<Long> ids(CreditResponse response) {
    if (response == null || response.getCast() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "TMDB returned invalid credits");
    }
    Set<Long> ids = new HashSet<>();
    for (Cast entry : response.getCast()) {
      if (entry == null || entry.getId() == null || entry.getId() <= 0) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "TMDB returned invalid credits");
      }
      ids.add(entry.getId());
    }
    return ids;
  }

  private ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
