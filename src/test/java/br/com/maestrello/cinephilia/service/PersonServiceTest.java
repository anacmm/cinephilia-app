package br.com.maestrello.cinephilia.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import br.com.maestrello.cinephilia.apis.PersonApi;
import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.CreditResponse;
import br.com.maestrello.cinephilia.model.CreditType;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.testutil.DataHelper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock private PersonApi personApi;

  @InjectMocks private PersonService personService;

  @Test
  void randomPersonUsesOnlyTop250AndCachesPoolUntilExpiry() {
    when(personApi.getPopularPeople(org.mockito.ArgumentMatchers.anyInt()))
        .thenAnswer(
            invocation -> {
              int page = invocation.getArgument(0);
              return br.com.maestrello.cinephilia.model.PersonResponse.builder()
                  .totalPages(100L)
                  .results(
                      java.util.stream.LongStream.rangeClosed((page - 1) * 20L + 1, page * 20L)
                          .mapToObj(id -> Person.builder().id(id).build())
                          .toList())
                  .build();
            });

    for (int i = 0; i < 1000; i++) {
      long id = personService.getRandomPerson().getId();
      assertTrue(id >= 1 && id <= 250);
    }
    for (int page = 1; page <= 13; page++) {
      org.mockito.Mockito.verify(personApi).getPopularPeople(page);
    }
    org.mockito.Mockito.verifyNoMoreInteractions(personApi);

    org.springframework.test.util.ReflectionTestUtils.setField(
        personService,
        "poolLoadedAt",
        System.nanoTime() - java.util.concurrent.TimeUnit.HOURS.toNanos(2));
    personService.getRandomPerson();
    org.mockito.Mockito.verify(personApi, org.mockito.Mockito.times(2)).getPopularPeople(1);
  }

  @Test
  void randomPersonHandlesSmallerPoolsAndDoesNotCacheEmptyResults() {
    when(personApi.getPopularPeople(1))
        .thenReturn(
            br.com.maestrello.cinephilia.model.PersonResponse.builder()
                .results(List.of())
                .totalPages(0L)
                .build())
        .thenReturn(
            br.com.maestrello.cinephilia.model.PersonResponse.builder()
                .results(List.of(DataHelper.person(), DataHelper.person()))
                .totalPages(1L)
                .build());

    var error =
        assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> personService.getRandomPerson());
    assertEquals(503, error.getStatusCode().value());
    assertEquals(DataHelper.person().getId(), personService.getRandomPerson().getId());
    org.mockito.Mockito.verify(personApi, org.mockito.Mockito.times(2)).getPopularPeople(1);
    org.mockito.Mockito.verifyNoMoreInteractions(personApi);
  }

  @Test
  void randomPersonDoesNotCachePartialRefreshOnFailure() {
    when(personApi.getPopularPeople(1))
        .thenReturn(
            br.com.maestrello.cinephilia.model.PersonResponse.builder()
                .results(List.of(DataHelper.person()))
                .totalPages(2L)
                .build());
    when(personApi.getPopularPeople(2)).thenThrow(new IllegalStateException("Unavailable"));
    assertThrows(IllegalStateException.class, () -> personService.getRandomPerson());
    assertThrows(IllegalStateException.class, () -> personService.getRandomPerson());
    org.mockito.Mockito.verify(personApi, org.mockito.Mockito.times(2)).getPopularPeople(1);
  }

  @Test
  void getPopularPeople_shouldReturnPersonList() {
    when(personApi.getPopularPeople()).thenReturn(DataHelper.personResponse());

    List<Person> result = personService.getPopularPeople();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(DataHelper.person().getName(), result.get(0).getName());
  }

  @Test
  void getPersonDetails_shouldReturnPerson() {
    when(personApi.getPerson(1L)).thenReturn(DataHelper.person());

    Person result = personService.getPersonDetails(1L);

    assertNotNull(result);
    assertEquals(DataHelper.person().getId(), result.getId());
  }

  @Test
  void getPersonFilmography_shouldReturnCombinedAndSortedCredits() {
    when(personApi.getMovieCredits(1L)).thenReturn(DataHelper.movieCreditResponse());
    when(personApi.getTvCredits(1L)).thenReturn(DataHelper.tvCreditResponse());

    List<Credit> result = personService.getPersonFilmography(1L);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.get(0).getReleaseDate().compareTo(result.get(1).getReleaseDate()) > 0);
  }

  @Test
  void filmographyDistinguishesMovieAndTvWithTheSameId() {
    when(personApi.getMovieCredits(1L))
        .thenReturn(
            CreditResponse.builder()
                .cast(
                    List.of(
                        Cast.builder().id(603L).name("Movie").releaseDate("1999-03-31").build()))
                .build());
    when(personApi.getTvCredits(1L))
        .thenReturn(
            CreditResponse.builder()
                .cast(
                    List.of(
                        Cast.builder().id(603L).name("TV show").firstAirDate("2008-01-20").build()))
                .build());

    List<Credit> result = personService.getPersonFilmography(1L);

    assertEquals(2, result.size());
    assertEquals(603L, result.get(0).getId());
    assertEquals(603L, result.get(1).getId());
    assertEquals(CreditType.TV, result.get(0).getCreditType());
    assertEquals(CreditType.MOVIE, result.get(1).getCreditType());
    assertEquals("TV show", result.get(0).getTitle());
    assertEquals("Movie", result.get(1).getTitle());
  }

  @Test
  void filmographySortsLatestFirstWithNullDatesLast() {
    when(personApi.getMovieCredits(6384L))
        .thenReturn(
            CreditResponse.builder()
                .cast(
                    List.of(
                        Cast.builder().id(1L).releaseDate(null).build(),
                        Cast.builder().id(2L).releaseDate("1999-03-31").build(),
                        Cast.builder().id(3L).releaseDate("2024-01-01").build(),
                        Cast.builder().id(4L).releaseDate(null).build()))
                .build());
    when(personApi.getTvCredits(6384L))
        .thenReturn(
            CreditResponse.builder()
                .cast(
                    List.of(
                        Cast.builder().id(5L).firstAirDate("2025-01-01").build(),
                        Cast.builder().id(6L).firstAirDate(null).build()))
                .build());

    List<Credit> result = personService.getPersonFilmography(6384L);

    assertEquals(List.of(5L, 3L, 2L, 1L, 4L, 6L), result.stream().map(Credit::getId).toList());
    assertTrue(result.subList(3, 6).stream().allMatch(credit -> credit.getReleaseDate() == null));
  }
}
