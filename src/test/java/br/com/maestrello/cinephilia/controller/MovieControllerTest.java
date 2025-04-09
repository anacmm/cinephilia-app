package br.com.maestrello.cinephilia.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.maestrello.cinephilia.stub.WireMockStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8081)
class MovieControllerTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setupWireMockStub() {
    WireMockStubs.stubMovieEndpoints();
  }

  @Test
  void getMovieDetails_shouldReturnMovieJson_whenMovieExists() throws Exception {
    // Arrange
    Long movieId = 1L;

    // Act & Assert
    mockMvc
        .perform(get("/movies/{movie_id}", movieId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.original_title").exists())
        .andExpect(jsonPath("$.original_title").isNotEmpty());
  }

  @Test
  void getPopularMovies_shouldReturnList_whenAvailable() throws Exception {
    mockMvc
        .perform(get("/movies/popular"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].original_title").exists());
  }

  @Test
  void getTopRatedMovies_shouldReturnList_whenAvailable() throws Exception {
    mockMvc
        .perform(get("/movies/top_rated"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].original_title").exists());
  }
}
