package br.com.maestrello.cinephilia.testutil;

import br.com.maestrello.cinephilia.model.Cast;
import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.CreditResponse;
import br.com.maestrello.cinephilia.model.CreditType;
import br.com.maestrello.cinephilia.model.Movie;
import br.com.maestrello.cinephilia.model.MovieResponse;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.model.PersonResponse;
import br.com.maestrello.cinephilia.model.TvShow;

import java.util.Collections;
import java.util.List;

public class DataHelper {

    public static Movie movie() {
        return Movie.builder()
                .id(1L)
                .originalTitle("Test Movie")
                .build();
    }

    public static MovieResponse movieResponse() {
        return MovieResponse.builder()
                .results(List.of(movie()))
                .build();
    }

    public static Person person() {
        return Person.builder()
                .id(1L)
                .name("Test Person")
                .build();
    }

    public static PersonResponse personResponse() {
        return PersonResponse.builder()
                .results(List.of(person()))
                .build();
    }

    public static Credit credit() {
        return Credit.builder()
                .id(1L)
                .creditType(CreditType.TV)
                .title("Test Credit")
                .build();
    }

    public static Cast cast() {
        return Cast.builder()
                .id(1L)
                .name("Test Cast")
                .build();
    }

    public static CreditResponse creditResponse() {
        return CreditResponse.builder()
                .cast(List.of(cast()))
                .build();
    }

    public static TvShow tvShow() {
        return TvShow.builder()
                .id(1L)
                .name("Test TV Show")
                .build();
    }

    public static List<TvShow> tvShowList() {
        return List.of(tvShow(), tvShow());
    }
}
