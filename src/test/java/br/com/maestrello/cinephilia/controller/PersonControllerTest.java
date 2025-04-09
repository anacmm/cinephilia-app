package br.com.maestrello.cinephilia.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.maestrello.cinephilia.testutil.WireMockStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8081)
class PersonControllerTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setupWireMockStub() {
    WireMockStubs.stubPersonEndpoints();
    WireMockStubs.stubTvEndpoints();
  }

  @Test
  void getPopularPeople_shouldReturnList_whenAvailable() throws Exception {
    mockMvc
        .perform(get("/person/popular"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").exists());
  }

  @Test
  void getPersonDetails_shouldReturnPerson_whenExists() throws Exception {
    mockMvc
        .perform(get("/person/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").exists());
  }

  @Test
  void getCredits_shouldReturnCredits_whenAvailable() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/person/1/credits"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].title").exists());
  }
}
