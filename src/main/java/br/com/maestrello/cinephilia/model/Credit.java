package br.com.maestrello.cinephilia.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Credit {

    Long id;
    String title;
    CreditType creditType;
    String posterPath;
    String releaseDate;
}
