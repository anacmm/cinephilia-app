package br.com.maestrello.cinephilia.service;

import br.com.maestrello.cinephilia.apis.PersonApi;
import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.CreditType;
import br.com.maestrello.cinephilia.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonApi personApi;

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
        credits.addAll(movieCredits.stream()
                .map(movie -> Credit.builder()
                        .id(movie.getId())
                        .title(movie.getName())
                        .creditType(CreditType.MOVIE)
                        .releaseDate(movie.getReleaseDate())
                        .posterPath(movie.getPosterPath())
                        .build())
                .toList());

        // Convert TV shows using Builder
        credits.addAll(tvCredits.stream()
                .map(tv -> Credit.builder()
                        .id(tv.getId())
                        .title(tv.getName())
                        .creditType(CreditType.TV)
                        .releaseDate(tv.getFirstAirDate())
                        .posterPath(tv.getPosterPath())
                        .build())
                .toList());

        // Sort by releaseDate (null-safe)
        return credits.stream()
                .sorted((c1, c2) -> {
                    if (c1.getReleaseDate() == null) return 1;
                    if (c2.getReleaseDate() == null) return -1;
                    return c1.getReleaseDate().compareTo(c2.getReleaseDate());
                })
                .toList();
    }
}