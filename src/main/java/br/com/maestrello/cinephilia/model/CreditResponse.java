package br.com.maestrello.cinephilia.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {

  private Long id;
  private List<Cast> cast;
  private List<CrewMember> crew;
}
