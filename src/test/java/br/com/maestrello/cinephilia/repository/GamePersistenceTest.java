package br.com.maestrello.cinephilia.repository;

import static org.junit.jupiter.api.Assertions.*;

import br.com.maestrello.cinephilia.model.GameSession;
import br.com.maestrello.cinephilia.model.GameSessionStatus;
import br.com.maestrello.cinephilia.model.Player;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag("database")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.autoconfigure.exclude=")
@ActiveProfiles("test")
@Transactional
class GamePersistenceTest {

  @Autowired private PlayerRepository players;
  @Autowired private GameSessionRepository sessions;
  @Autowired private EntityManager entityManager;
  @Autowired private DataSource dataSource;

  @Test
  void persistsAndRetrievesPlayerWithGeneratedIdAndTimestamp() {
    Player player = players.saveAndFlush(new Player("Guest"));
    assertNotNull(player.getId());
    assertNotNull(player.getCreatedAt());
    var createdAt = player.getCreatedAt();
    entityManager.clear();

    Player loaded = players.findById(player.getId()).orElseThrow();
    assertEquals("Guest", loaded.getDisplayName());
    assertEquals(createdAt, loaded.getCreatedAt());
    loaded.setDisplayName("Renamed guest");
    players.flush();
    entityManager.clear();
    assertEquals(createdAt, players.findById(player.getId()).orElseThrow().getCreatedAt());
  }

  @Test
  void differentPlayersCanShareDisplayName() {
    Player first = players.saveAndFlush(new Player("Guest"));
    Player second = players.saveAndFlush(new Player("Guest"));
    entityManager.clear();

    assertNotEquals(first.getId(), second.getId());
    assertEquals("Guest", players.findById(first.getId()).orElseThrow().getDisplayName());
    assertEquals("Guest", players.findById(second.getId()).orElseThrow().getDisplayName());
  }

  @Test
  void persistsInProgressSessionWithNullableCompletionFields() {
    Player player = players.saveAndFlush(new Player("Guest"));
    GameSession session = sessions.saveAndFlush(new GameSession(player, 6384L, 287L));
    assertNotNull(session.getId());
    assertNotNull(session.getStartedAt());
    entityManager.clear();

    GameSession loaded = sessions.findById(session.getId()).orElseThrow();
    assertEquals(player.getId(), loaded.getPlayer().getId());
    assertEquals(6384L, loaded.getStartPersonId());
    assertEquals(287L, loaded.getTargetPersonId());
    assertEquals(GameSessionStatus.IN_PROGRESS, loaded.getStatus());
    assertEquals(session.getStartedAt(), loaded.getStartedAt());
    assertNull(loaded.getCompletedAt());
    assertNull(loaded.getMoveCount());
    assertNull(loaded.getDurationMs());
    assertNull(loaded.getPath());
  }

  @Test
  void completesSessionAndRoundTripsOrderedJsonbPath() {
    Player player = players.saveAndFlush(new Player("Guest"));
    GameSession session = sessions.saveAndFlush(new GameSession(player, 6384L, 287L));
    var startedAt = session.getStartedAt();
    var completedAt = startedAt.plus(45, ChronoUnit.SECONDS);
    List<Map<String, Object>> path =
        List.of(
            Map.of("type", "PERSON", "id", 6384, "name", "Keanu Reeves"),
            Map.of("type", "MOVIE", "id", 603, "metadata", Map.of("title", "The Matrix")),
            Map.of("type", "PERSON", "id", 287));
    session.setStatus(GameSessionStatus.COMPLETED);
    session.setCompletedAt(completedAt);
    session.setMoveCount(2);
    session.setDurationMs(45000L);
    session.setPath(path);
    sessions.saveAndFlush(session);
    entityManager.clear();

    GameSession loaded = sessions.findById(session.getId()).orElseThrow();
    assertEquals(GameSessionStatus.COMPLETED, loaded.getStatus());
    assertEquals(startedAt, loaded.getStartedAt());
    assertEquals(completedAt, loaded.getCompletedAt());
    assertEquals(2, loaded.getMoveCount());
    assertEquals(45000L, loaded.getDurationMs());
    assertEquals(path, loaded.getPath());
    assertEquals(
        "array",
        entityManager
            .createNativeQuery(
                "SELECT jsonb_typeof(path) FROM game_sessions WHERE id = :id", String.class)
            .setParameter("id", session.getId())
            .getSingleResult());
  }

  @Test
  void canAbandonSessionWithoutCompletionData() {
    Player player = players.saveAndFlush(new Player("Guest"));
    GameSession session = sessions.saveAndFlush(new GameSession(player, 6384L, 287L));
    session.setStatus(GameSessionStatus.ABANDONED);
    sessions.flush();
    entityManager.clear();
    assertEquals(
        GameSessionStatus.ABANDONED, sessions.findById(session.getId()).orElseThrow().getStatus());
  }

  @Test
  void databaseEnforcesNameAndForeignKeyConstraints() throws Exception {
    // Separate autocommit connections let each rejection be verified without poisoning the test
    // transaction.
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      for (String value : List.of("NULL", "''", "'   '", "repeat('x', 51)")) {
        assertThrows(
            SQLException.class,
            () ->
                statement.executeUpdate(
                    "INSERT INTO players (display_name) VALUES (" + value + ")"));
      }
      SQLException missingPlayer =
          assertThrows(
              SQLException.class,
              () ->
                  statement.executeUpdate(
                      "INSERT INTO game_sessions (player_id, start_person_id, target_person_id) "
                          + "VALUES (gen_random_uuid(), 6384, 287)"));
      assertEquals("23503", missingPlayer.getSQLState());
    }
  }

  @Test
  void databaseRejectsInvalidSessionFields() throws Exception {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      for (String values :
          List.of(
              "id, NULL, 287, 'IN_PROGRESS', NULL, NULL, NULL",
              "id, 6384, NULL, 'IN_PROGRESS', NULL, NULL, NULL",
              "NULL, 6384, 287, 'IN_PROGRESS', NULL, NULL, NULL",
              "id, 0, 287, 'IN_PROGRESS', NULL, NULL, NULL",
              "id, 6384, -1, 'IN_PROGRESS', NULL, NULL, NULL",
              "id, 6384, 287, NULL, NULL, NULL, NULL",
              "id, 6384, 287, 'INVALID', NULL, NULL, NULL",
              "id, 6384, 287, 'IN_PROGRESS', -1, NULL, NULL",
              "id, 6384, 287, 'IN_PROGRESS', NULL, -1, NULL",
              "id, 6384, 287, 'COMPLETED', 1, 1000, CURRENT_TIMESTAMP - INTERVAL '1 day'")) {
        SQLException error =
            assertThrows(
                SQLException.class,
                () ->
                    statement.executeUpdate(
                        "WITH p AS (INSERT INTO players (display_name) VALUES ('Constraint test') RETURNING id) "
                            + "INSERT INTO game_sessions (player_id, start_person_id, target_person_id, status, "
                            + "move_count, duration_ms, completed_at) SELECT "
                            + values
                            + " FROM p"));
        assertTrue(error.getSQLState().startsWith("23"));
      }
    }
  }
}
