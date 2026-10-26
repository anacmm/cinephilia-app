package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.CompleteGameRequest;
import br.com.maestrello.cinephilia.model.GameResponse;
import br.com.maestrello.cinephilia.model.StartGameRequest;
import br.com.maestrello.cinephilia.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
  private final GameService gameService;

  @Operation(summary = "Start a guest game")
  @PostMapping
  public ResponseEntity<GameResponse> start(@Valid @RequestBody StartGameRequest request) {
    GameResponse game = gameService.start(request);
    return ResponseEntity.created(URI.create("/games/" + game.id())).body(game);
  }

  @Operation(summary = "Retrieve a game")
  @GetMapping("/{id}")
  public GameResponse get(@PathVariable UUID id) {
    return gameService.get(id);
  }

  @Operation(summary = "Validate a navigation path and complete a game")
  @PostMapping("/{id}/complete")
  public GameResponse complete(
      @PathVariable UUID id, @Valid @RequestBody CompleteGameRequest request) {
    return gameService.complete(id, request);
  }
}
