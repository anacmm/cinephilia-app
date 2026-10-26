package br.com.maestrello.cinephilia.repository;

import br.com.maestrello.cinephilia.model.Player;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, UUID> {}
