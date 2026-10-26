package br.com.maestrello.cinephilia.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.maestrello.cinephilia.model.GameSessionStatus;
import br.com.maestrello.cinephilia.repository.GameSessionRepository;
import br.com.maestrello.cinephilia.repository.PlayerRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@Tag("database")
@SpringBootTest(properties = "spring.autoconfigure.exclude=")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(port = 8081))
class GameControllerTest {
  private static final String VALID_PATH =
      """
      {"path":[{"type":"PERSON","id":1},{"type":"MOVIE","id":10},{"type":"PERSON","id":2}]}
      """;
  @Autowired private MockMvc mvc;
  @Autowired private GameSessionRepository games;
  @Autowired private PlayerRepository players;
  @MockitoBean private Clock gameClock;
  private final List<UUID> createdGames = new ArrayList<>();

  @BeforeEach
  void setup() {
    reset();
    when(gameClock.instant()).thenAnswer(invocation -> Instant.now());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/person/1/movie_credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":10}]}")));
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":1},{\"id\":2}]}")));
  }

  @AfterEach
  void cleanup() {
    for (UUID id : createdGames) {
      games
          .findById(id)
          .ifPresent(
              game -> {
                UUID playerId = game.getPlayer().getId();
                games.deleteById(id);
                players.deleteById(playerId);
              });
    }
  }

  private UUID start() throws Exception {
    String json =
        mvc.perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"playerName\":\"Guest\",\"startPersonId\":1,\"targetPersonId\":2}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.playerName").value("Guest"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.startPersonId").value(1))
            .andExpect(jsonPath("$.targetPersonId").value(2))
            .andExpect(jsonPath("$.startedAt").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID id = UUID.fromString(JsonPath.read(json, "$.id"));
    createdGames.add(id);
    return id;
  }

  @Test
  void createsAndRetrievesDistinctGuestGames() throws Exception {
    UUID first = start();
    UUID second = start();
    assertNotEquals(first, second);
    assertNotEquals(
        games.findById(first).orElseThrow().getPlayer().getId(),
        games.findById(second).orElseThrow().getPlayer().getId());
    mvc.perform(get("/games/{id}", first))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(first.toString()))
        .andExpect(jsonPath("$.playerName").value("Guest"))
        .andExpect(jsonPath("$.completedAt").isEmpty())
        .andExpect(jsonPath("$.moveCount").isEmpty())
        .andExpect(jsonPath("$.durationMs").isEmpty());
    verify(0, getRequestedFor(urlMatching("/person/.*")));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{}",
        "{\"playerName\":\" \",\"startPersonId\":1,\"targetPersonId\":2}",
        "{\"playerName\":\"Guest\",\"startPersonId\":0,\"targetPersonId\":2}",
        "{\"playerName\":\"Guest\",\"startPersonId\":1,\"targetPersonId\":-2}",
        "{\"playerName\":\"Guest\",\"startPersonId\":1,\"targetPersonId\":1}",
        "{\"playerName\":\"123456789012345678901234567890123456789012345678901\",\"startPersonId\":1,\"targetPersonId\":2}"
      })
  void rejectsInvalidStartWithoutCreatingPlayer(String body) throws Exception {
    long count = players.count();
    mvc.perform(post("/games").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
    assertEquals(count, players.count());
  }

  @Test
  void returns404ForUnknownGameAnd400ForMalformedId() throws Exception {
    mvc.perform(get("/games/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    mvc.perform(
            post("/games/{id}/complete", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isNotFound());
    mvc.perform(get("/games/not-a-uuid")).andExpect(status().isBadRequest());
  }

  @Test
  void completesWithServerDurationAndCountAndRejectsDuplicate() throws Exception {
    UUID id = start();
    Instant startedAt = games.findById(id).orElseThrow().getStartedAt();
    Instant finished = startedAt.plusMillis(12345);
    when(gameClock.instant()).thenReturn(finished.plusNanos(987));
    String forged =
        VALID_PATH
            .stripTrailing()
            .replaceFirst(
                "}$",
                ",\"moveCount\":999,\"durationMs\":1,\"completedAt\":\"2000-01-01T00:00:00Z\"}");
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(forged))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.moveCount").value(2))
        .andExpect(jsonPath("$.durationMs").value(12345))
        .andExpect(jsonPath("$.path.length()").value(3));
    var saved = games.findById(id).orElseThrow();
    assertEquals(finished, saved.getCompletedAt());
    assertEquals(startedAt, saved.getStartedAt());
    assertEquals(3, saved.getPath().size());
    mvc.perform(get("/games/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moveCount").value(2))
        .andExpect(jsonPath("$.durationMs").value(12345));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isConflict());
    verify(1, getRequestedFor(urlPathEqualTo("/movie/10/credits")));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"path\":[]}",
        "{\"path\":null}",
        "{\"path\":[null,null,null]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":1},{\"type\":\"MOVIE\",\"id\":10},{\"type\":\"PERSON\",\"id\":3}]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":3},{\"type\":\"MOVIE\",\"id\":10},{\"type\":\"PERSON\",\"id\":2}]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":1},{\"type\":\"PERSON\",\"id\":10},{\"type\":\"PERSON\",\"id\":2}]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":1},{\"type\":\"MOVIE\",\"id\":10}]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":1},{\"type\":\"INVALID\",\"id\":10},{\"type\":\"PERSON\",\"id\":2}]}",
        "{\"path\":[{\"type\":0,\"id\":1},{\"type\":\"MOVIE\",\"id\":10},{\"type\":\"PERSON\",\"id\":2}]}",
        "{\"path\":[{\"type\":\"PERSON\",\"id\":1},{\"type\":\"MOVIE\",\"id\":0},{\"type\":\"PERSON\",\"id\":2}]}",
        "{bad json}"
      })
  void rejectsMalformedAndUnfinishedPaths(String body) throws Exception {
    UUID id = start();
    mvc.perform(
            post("/games/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
    assertUncompleted(id);
    verify(0, getRequestedFor(urlMatching("/person/.*")));
  }

  @Test
  void rejectsFractionalIds() throws Exception {
    UUID id = start();
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH.replace("10", "10.5")))
        .andExpect(status().isBadRequest());
    assertUncompleted(id);
    mvc.perform(
            post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerName\":\"Guest\",\"startPersonId\":1.5,\"targetPersonId\":2}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refusesAbandonedSessionAndBackwardServerClock() throws Exception {
    UUID id = start();
    var game = games.findById(id).orElseThrow();
    when(gameClock.instant()).thenReturn(game.getStartedAt().minusSeconds(1));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isServiceUnavailable());
    assertUncompleted(id);
    game.setStatus(GameSessionStatus.ABANDONED);
    games.saveAndFlush(game);
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isConflict());
  }

  @Test
  void missingTmdbResourceIsAnInvalidPath() throws Exception {
    UUID id = start();
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/person/1/movie_credits"))
            .willReturn(aResponse().withStatus(404)));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isBadRequest());
    assertUncompleted(id);
  }

  @Test
  void rejectsDisconnectedPersonAndCastTransitions() throws Exception {
    UUID id = start();
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/person/1/movie_credits"))
            .willReturn(okJson("{\"cast\":[]}")));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isBadRequest());
    assertUncompleted(id);
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/person/1/movie_credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":10}]}")));
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":1}]}")));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isBadRequest());
    assertUncompleted(id);
  }

  @Test
  void allowsRevisitsWithOneLookupPerPersonTypeAndProduction() throws Exception {
    UUID id = start();
    String path =
        """
        {"path":[{"type":"PERSON","id":1},{"type":"MOVIE","id":10},{"type":"PERSON","id":1},
        {"type":"MOVIE","id":10},{"type":"PERSON","id":2}]}
        """;
    mvc.perform(
            post("/games/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content(path))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moveCount").value(4));
    verify(1, getRequestedFor(urlPathEqualTo("/person/1/movie_credits")));
    verify(1, getRequestedFor(urlPathEqualTo("/movie/10/credits")));
  }

  @Test
  void distinguishesMovieAndTvWithSameId() throws Exception {
    UUID id = start();
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/person/1/tv_credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":10}]}")));
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/tv/10/credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":2}]}")));
    String path =
        """
        {"path":[{"type":"PERSON","id":1},{"type":"MOVIE","id":10},{"type":"PERSON","id":1},
        {"type":"TV","id":10},{"type":"PERSON","id":2}]}
        """;
    mvc.perform(
            post("/games/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content(path))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moveCount").value(4));
    verify(1, getRequestedFor(urlPathEqualTo("/movie/10/credits")));
    verify(1, getRequestedFor(urlPathEqualTo("/tv/10/credits")));
  }

  @Test
  void tmdbFailuresLeaveSessionUnchangedAndCanBeRetried() throws Exception {
    UUID id = start();
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(aResponse().withStatus(503)));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isServiceUnavailable());
    assertUncompleted(id);
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(okJson("{}")));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isBadGateway());
    assertUncompleted(id);
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":2}]}")));
    mvc.perform(
            post("/games/{id}/complete", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PATH))
        .andExpect(status().isOk());
  }

  @Test
  void concurrentCompletionsProduceOneSuccessAndOneConflict() throws Exception {
    UUID id = start();
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/movie/10/credits"))
            .willReturn(okJson("{\"cast\":[{\"id\":2}]}").withFixedDelay(250)));
    CountDownLatch begin = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var task =
          (java.util.concurrent.Callable<Integer>)
              () -> {
                begin.await();
                return mvc.perform(
                        post("/games/{id}/complete", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_PATH))
                    .andReturn()
                    .getResponse()
                    .getStatus();
              };
      var first = executor.submit(task);
      var second = executor.submit(task);
      begin.countDown();
      var statuses = List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
      assertTrue(statuses.contains(200));
      assertTrue(statuses.contains(409));
    }
    verify(1, getRequestedFor(urlPathEqualTo("/movie/10/credits")));
    assertEquals(GameSessionStatus.COMPLETED, games.findById(id).orElseThrow().getStatus());
  }

  private void assertUncompleted(UUID id) {
    var saved = games.findById(id).orElseThrow();
    assertEquals(GameSessionStatus.IN_PROGRESS, saved.getStatus());
    assertNull(saved.getCompletedAt());
    assertNull(saved.getMoveCount());
    assertNull(saved.getDurationMs());
    assertNull(saved.getPath());
  }
}
