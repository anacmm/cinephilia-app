package br.com.maestrello.cinephilia.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import br.com.maestrello.cinephilia.apis.PersonApi;
import br.com.maestrello.cinephilia.model.Credit;
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
    assertTrue(result.get(0).getReleaseDate().compareTo(result.get(1).getReleaseDate()) < 0);
  }
}
