package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.PersonApi;
import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.CreditType;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.model.PersonResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonApi personApi;

  private List<Person> randomPeople = List.of();
  private long poolLoadedAt;

  public synchronized Person getRandomPerson() {
    if (randomPeople.isEmpty() || System.nanoTime() - poolLoadedAt >= TimeUnit.HOURS.toNanos(1)) {
      // Publish only complete refreshes, so failed upstream calls never cache a partial pool.
      var people = new LinkedHashMap<Long, Person>();
      for (int page = 1; page <= 13 && people.size() < 250; page++) {
        PersonResponse response = personApi.getPopularPeople(page);
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
          break;
        }
        for (Person person : response.getResults()) {
          if (person != null && person.getId() != null) {
            people.putIfAbsent(person.getId(), person);
          }
          if (people.size() == 250) {
            break;
          }
        }
        if (response.getTotalPages() != null && page >= response.getTotalPages()) {
          break;
        }
      }
      if (people.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, "No popular people available");
      }
      randomPeople = List.copyOf(people.values());
      poolLoadedAt = System.nanoTime();
    }
    return randomPeople.get(ThreadLocalRandom.current().nextInt(randomPeople.size()));
  }

  public List<Person> getPopularPeople() {
    return personApi.getPopularPeople().getResults();
  }

  public Person getPersonDetails(Long personId) {
    return personApi.getPerson(personId);
  }

  public List<Credit> getPersonFilmography(Long personId) {
    // Fetch movie and TV credits
    List<Cast> movieCredits = personApi.getMovieCredits(personId).getCast();
    List<Cast> tvCredits = personApi.getTvCredits(personId).getCast();

    // Convert movies using Builder
    List<Credit> credits = new ArrayList<>();
    credits.addAll(
        movieCredits.stream()
            .map(
                movie ->
                    Credit.builder()
                        .id(movie.getId())
                        .title(movie.getName())
                        .creditType(CreditType.MOVIE)
                        .releaseDate(movie.getReleaseDate())
                        .posterPath(movie.getPosterPath())
                        .build())
            .toList());

    // Convert TV shows using Builder
    credits.addAll(
        tvCredits.stream()
            .map(
                tv ->
                    Credit.builder()
                        .id(tv.getId())
                        .title(tv.getName())
                        .creditType(CreditType.TV)
                        .releaseDate(tv.getFirstAirDate())
                        .posterPath(tv.getPosterPath())
                        .build())
            .toList());

    // Latest release dates first, with missing dates last.
    return credits.stream()
        .sorted(
            Comparator.comparing(
                Credit::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
  }
}
