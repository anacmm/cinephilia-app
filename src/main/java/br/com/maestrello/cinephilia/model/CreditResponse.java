package br.com.maestrello.cinephilia.model;

import lombok.Data;

import java.util.List;

@Data
public class CreditResponse {

    private Long id;
    private List<Cast> cast;
}
