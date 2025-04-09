package br.com.maestrello.cinephilia.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditResponse {

  private Long id;
  private List<Cast> cast;
}
