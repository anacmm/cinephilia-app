package br.com.maestrello.cinephilia.repository;

import br.com.maestrello.cinephilia.model.GameSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select game from GameSession game where game.id = :id")
  Optional<GameSession> findForCompletion(@Param("id") UUID id);
}
