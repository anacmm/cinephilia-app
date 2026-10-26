package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.model.*;
import br.com.maestrello.cinephilia.repository.GameSessionRepository;
import br.com.maestrello.cinephilia.repository.PlayerRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GameService {
  private final PlayerRepository players;
  private final GameSessionRepository games;
  private final GamePathValidator pathValidator;
  private final Clock gameClock;

  @Transactional
  public GameResponse start(StartGameRequest request) {
    if (request.startPersonId().equals(request.targetPersonId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "START and TARGET must be different people");
    }
    Player player = players.save(new Player(request.playerName()));
    return GameResponse.from(
        games.saveAndFlush(
            new GameSession(player, request.startPersonId(), request.targetPersonId())));
  }

  @Transactional(readOnly = true)
  public GameResponse get(UUID id) {
    return GameResponse.from(games.findById(id).orElseThrow(this::notFound));
  }

  @Transactional
  public GameResponse complete(UUID id, CompleteGameRequest request) {
    GameSession game = games.findForCompletion(id).orElseThrow(this::notFound);
    if (game.getStatus() != GameSessionStatus.IN_PROGRESS) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is no longer IN_PROGRESS");
    }
    pathValidator.validate(game, request.path());
    // PostgreSQL timestamps have microsecond precision; return the same instant that is stored.
    var completedAt = gameClock.instant().truncatedTo(ChronoUnit.MICROS);
    if (completedAt.isBefore(game.getStartedAt())) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Server clock precedes game start");
    }
    List<Map<String, Object>> path =
        request.path().stream()
            .map(
                selection ->
                    Map.<String, Object>of("type", selection.type().name(), "id", selection.id()))
            .toList();
    game.setPath(path);
    game.setMoveCount(path.size() - 1);
    game.setCompletedAt(completedAt);
    game.setDurationMs(Duration.between(game.getStartedAt(), completedAt).toMillis());
    game.setStatus(GameSessionStatus.COMPLETED);
    return GameResponse.from(games.saveAndFlush(game));
  }

  private ResponseStatusException notFound() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found");
  }
}
