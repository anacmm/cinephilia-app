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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PersonServiceTest {

  @Mock private PersonApi personApi;

  @InjectMocks private PersonService personService;

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
