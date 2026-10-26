package br.com.maestrello.cinephilia.config;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("database")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.autoconfigure.exclude=")
@ActiveProfiles("test")
class DatabaseFoundationTest {

  @Autowired private DataSource dataSource;
  @Autowired private Flyway flyway;
  @Autowired private EntityManagerFactory entityManagerFactory;

  @Test
  void startsWithPostgresAndValidatesMigratedSchema() throws Exception {
    try (Connection connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
      try (var result = statement.executeQuery("SELECT 1")) {
        assertTrue(result.next());
        assertEquals(1, result.getInt(1));
      }
      try (var result =
          statement.executeQuery(
              "SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success")) {
        assertTrue(result.next());
        assertEquals(1, result.getInt(1));
      }
      try (var result =
          statement.executeQuery(
              "SELECT table_name FROM information_schema.tables "
                  + "WHERE table_schema = current_schema() AND table_type = 'BASE TABLE' "
                  + "ORDER BY table_name")) {
        assertTrue(result.next());
        assertEquals("flyway_schema_history", result.getString(1));
        assertTrue(result.next());
        assertEquals("game_sessions", result.getString(1));
        assertTrue(result.next());
        assertEquals("players", result.getString(1));
        assertFalse(result.next());
      }
    }
    assertEquals("validate", entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto"));
    assertEquals(2, entityManagerFactory.getMetamodel().getEntities().size());
    flyway.validate();
    assertEquals("2", flyway.info().current().getVersion().getVersion());
    assertEquals(0, flyway.info().pending().length);
    assertEquals(
        0, flyway.migrate().migrationsExecuted, "Migrations must be repeatable on restart");
  }
}
