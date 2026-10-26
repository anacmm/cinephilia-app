package br.com.maestrello.cinephilia.repository;

import br.com.maestrello.cinephilia.model.GameSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {}
