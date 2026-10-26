package br.com.maestrello.cinephilia.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.maestrello.cinephilia.testutil.WireMockStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(port = 8081, filesUnderDirectory = "src/test/resources"))
class PersonControllerTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setupWireMockStub() {
    WireMockStubs.stubPersonEndpoints();
    WireMockStubs.stubTvEndpoints();
  }

  @Test
  void getRandomPersonReturns503ForEmptyPoolThenReturnsPerson() throws Exception {
    com.github.tomakehurst.wiremock.client.WireMock.stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/person/popular"))
            .withQueryParam("page", com.github.tomakehurst.wiremock.client.WireMock.equalTo("1"))
            .willReturn(
                com.github.tomakehurst.wiremock.client.WireMock.okJson(
                    "{\"results\":[],\"total_pages\":0}")));
    mockMvc.perform(get("/person/random")).andExpect(status().isServiceUnavailable());

    com.github.tomakehurst.wiremock.client.WireMock.stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/person/popular"))
            .withQueryParam("page", com.github.tomakehurst.wiremock.client.WireMock.equalTo("1"))
            .willReturn(
                com.github.tomakehurst.wiremock.client.WireMock.okJson(
                    "{\"results\":[{\"id\":6384,\"name\":\"Keanu Reeves\"}],\"total_pages\":1}")));
    mockMvc
        .perform(get("/person/random"))
        .andExpect(status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.id").value(6384))
        .andExpect(jsonPath("$.name").value("Keanu Reeves"));
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
        .andExpect(jsonPath("$[0].title").exists())
        .andExpect(
            jsonPath("$[?(@.creditType == 'MOVIE' && @.id == 2185)].title")
                .value(org.hamcrest.Matchers.hasItem("Big Trouble")))
        .andExpect(jsonPath("$[?(@.creditType == 'TV')].title").isNotEmpty());
  }
}
