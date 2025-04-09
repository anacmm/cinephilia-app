package br.com.maestrello.cinephilia.model;

import java.util.List;
import lombok.Data;

@Data
public class CreditResponse {

  private Long id;
  private List<Cast> cast;
}
