package br.com.maestrello.cinephilia.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(port = 8081, filesUnderDirectory = "src/test/resources"))
class CastControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private tools.jackson.databind.ObjectMapper objectMapper;

  @BeforeEach
  void resetStubs() {
    reset();
  }

  @Test
  void movieCastIncludesMatrixActors() throws Exception {
    stubCredits("/movie/603/credits", "movie_cast.json");
    mockMvc
        .perform(get("/movies/603/credits"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(
            jsonPath(
                "$[*].name", contains("Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss")))
        .andExpect(jsonPath("$[0].id").value(6384))
        .andExpect(jsonPath("$[0].character").value("Neo"))
        .andExpect(jsonPath("$[0].profile_path").value("/keanu.jpg"))
        .andExpect(jsonPath("$[0].order").value(0));
    verify(
        getRequestedFor(urlPathEqualTo("/movie/603/credits"))
            .withQueryParam("api_key", equalTo("1234")));
  }

  @Test
  void tvCastIncludesBreakingBadActors() throws Exception {
    stubCredits("/tv/1396/credits", "tv_cast.json");
    mockMvc
        .perform(get("/tv/1396/credits"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(17419))
        .andExpect(jsonPath("$[0].name").value("Bryan Cranston"))
        .andExpect(jsonPath("$[0].character").value("Walter White"))
        .andExpect(jsonPath("$[0].profile_path").value("/bryan.jpg"))
        .andExpect(jsonPath("$[0].order").value(0))
        .andExpect(jsonPath("$[1].name").value("Aaron Paul"))
        .andExpect(jsonPath("$[1].profile_path").value(nullValue()))
        .andExpect(jsonPath("$[1].character").value(nullValue()))
        .andExpect(jsonPath("$[1].order").value(nullValue()));
    verify(
        getRequestedFor(urlPathEqualTo("/tv/1396/credits"))
            .withQueryParam("api_key", equalTo("1234")));
  }

  @ParameterizedTest
  @CsvSource({"movies,movie", "tv,tv"})
  void emptyCastReturnsEmptyArray(String route, String upstream) throws Exception {
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/" + upstream + "/1/credits"))
            .willReturn(okJson("{\"id\":1,\"cast\":[],\"crew\":[{\"id\":2}]}")));
    mockMvc
        .perform(get("/" + route + "/1/credits"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @ParameterizedTest
  @CsvSource({"movies,movie,404", "tv,tv,404", "movies,movie,500", "tv,tv,500"})
  void upstreamErrorsPreserveStatusWithoutLeakingCredentials(
      String route, String upstream, int code) throws Exception {
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/" + upstream + "/9999/credits"))
            .willReturn(aResponse().withStatus(code)));
    mockMvc
        .perform(get("/" + route + "/9999/credits"))
        .andExpect(status().is(code))
        .andExpect(jsonPath("$.status").value(code))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("TMDB API error: " + code))
        .andExpect(content().string(not(containsString("api_key"))));
  }

  @Test
  void openApiIncludesBothCastEndpoints() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/movies/{movieId}/credits'].get.responses['200']").exists())
        .andExpect(jsonPath("$.paths['/tv/{tvId}/credits'].get.responses['200']").exists());
  }

  @ParameterizedTest
  @CsvSource({"movies,movie,603,movie_cast.json", "tv,tv,1396,tv_cast.json"})
  void detailsPreserveCastAndIncludeOnlyDirectors(
      String route, String upstream, long id, String fixture) throws Exception {
    stubCredits("/" + upstream + "/" + id + "/credits", fixture);
    String base = "/" + route + "/" + id + "/credits";
    String castJson =
        mockMvc
            .perform(get(base))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String detailsJson =
        mockMvc
            .perform(get(base + "/details"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.directors", hasSize(2)))
            .andExpect(jsonPath("$.directors[*].id", contains(100, 101)))
            .andExpect(
                jsonPath("$.directors[*].name", contains("First director", "Second director")))
            .andExpect(jsonPath("$.directors[0].profile_path").value("/director.jpg"))
            .andExpect(jsonPath("$.directors[1].profile_path").value(nullValue()))
            .andExpect(jsonPath("$.directors[0].job").doesNotExist())
            .andExpect(jsonPath("$.crew").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    org.junit.jupiter.api.Assertions.assertEquals(
        objectMapper.readTree(castJson), objectMapper.readTree(detailsJson).get("cast"));
    verify(
        2,
        getRequestedFor(urlPathEqualTo("/" + upstream + "/" + id + "/credits"))
            .withQueryParam("api_key", equalTo("1234")));
  }

  @ParameterizedTest
  @CsvSource({"movies,movie", "tv,tv"})
  void detailsHandleMissingEmptyOrNonDirectorCrew(String route, String upstream) throws Exception {
    for (String crew :
        new String[] {
          "", ",\"crew\":null", ",\"crew\":[]", ",\"crew\":[{\"id\":1,\"job\":\"Producer\"}]"
        }) {
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  urlPathEqualTo("/" + upstream + "/1/credits"))
              .willReturn(okJson("{\"cast\":[]" + crew + "}")));
      mockMvc
          .perform(get("/" + route + "/1/credits/details"))
          .andExpect(status().isOk())
          .andExpect(content().json("{\"cast\":[],\"directors\":[]}"));
    }
  }

  @ParameterizedTest
  @CsvSource({"movies,movie,404", "tv,tv,404", "movies,movie,500", "tv,tv,500"})
  void detailsPreserveUpstreamErrorHandling(String route, String upstream, int code)
      throws Exception {
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/" + upstream + "/1/credits"))
            .willReturn(aResponse().withStatus(code)));
    mockMvc
        .perform(get("/" + route + "/1/credits/details"))
        .andExpect(status().is(code))
        .andExpect(jsonPath("$.message").value("TMDB API error: " + code));
  }

  @Test
  void openApiDocumentsDetailedResponseAndPreservesArrayContract() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                    "$.paths['/movies/{movieId}/credits/details'].get.responses['200'].content['*/*'].schema['$ref']")
                .value("#/components/schemas/ProductionCredits"))
        .andExpect(jsonPath("$.paths['/tv/{tvId}/credits/details'].get.responses['200']").exists())
        .andExpect(
            jsonPath("$.components.schemas.ProductionCredits.properties.cast.type").value("array"))
        .andExpect(
            jsonPath("$.components.schemas.ProductionCredits.properties.directors.type")
                .value("array"))
        .andExpect(
            jsonPath(
                    "$.paths['/movies/{movieId}/credits'].get.responses['200'].content['*/*'].schema.type")
                .value("array"))
        .andExpect(
            jsonPath(
                    "$.paths['/tv/{tvId}/credits'].get.responses['200'].content['*/*'].schema.type")
                .value("array"));
  }

  private void stubCredits(String path, String fixture) {
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo(path))
            .withQueryParam("api_key", equalTo("1234"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile(fixture)));
  }
}
