package br.com.maestrello.cinephilia.testutil;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class WireMockStubs {

  private static final String API_KEY_REGEX = ".+";

  public static void stubMovieEndpoints() {
    stubFor(
        get(urlPathMatching("/movie/\\d+"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("movie.json")));

    stubFor(
        get(urlPathEqualTo("/movie/popular"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("movies-popular.json")));

    stubFor(
        get(urlPathEqualTo("/movie/top_rated"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("movies-top-rated.json")));
  }

  public static void stubPersonEndpoints() {
    stubFor(
        get(urlPathMatching("/person/\\d+"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("person.json")));

    stubFor(
        get(urlPathMatching("/person/\\d+/movie_credits"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("person_movie_credits.json")));

    stubFor(
        get(urlPathMatching("/person/\\d+/tv_credits"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("person_tv_credits.json")));

    stubFor(
        get(urlPathEqualTo("/person/popular"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("person_popular.json")));
  }

  public static void stubTvEndpoints() {
    stubFor(
        get(urlPathEqualTo("/tv/popular"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("tv_popular.json")));

    stubFor(
        get(urlPathEqualTo("/tv/top_rated"))
            .withQueryParam("api_key", matching(API_KEY_REGEX))
            .willReturn(okJsonFile("tv_top_rated.json")));
  }

  private static ResponseDefinitionBuilder okJsonFile(String fileName) {
    return aResponse()
        .withStatus(200)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withBodyFile(fileName);
  }

}
