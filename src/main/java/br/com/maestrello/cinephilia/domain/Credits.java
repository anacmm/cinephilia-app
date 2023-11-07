package br.com.maestrello.cinephilia.domain;

import lombok.Data;

import java.util.List;

@Data
public class Credits {

    private Long id;
    private List<Cast> cast;
}
