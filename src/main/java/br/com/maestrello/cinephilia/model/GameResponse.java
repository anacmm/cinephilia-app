package br.com.maestrello.cinephilia.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameResponse(
    UUID id,
    String playerName,
    Long startPersonId,
    Long targetPersonId,
    GameSessionStatus status,
    Instant startedAt,
    Instant completedAt,
    Integer moveCount,
    Long durationMs,
    List<Map<String, Object>> path) {

  public static GameResponse from(GameSession game) {
    return new GameResponse(
        game.getId(),
        game.getPlayer().getDisplayName(),
        game.getStartPersonId(),
        game.getTargetPersonId(),
        game.getStatus(),
        game.getStartedAt(),
        game.getCompletedAt(),
        game.getMoveCount(),
        game.getDurationMs(),
        game.getPath());
  }
}
