package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("person")
@RequiredArgsConstructor
public class PersonController {

  private final PersonService personService;

  @Operation(
      summary = "Get popular people",
      description = "Returns a list of popular people from TMDB.")
  @CinephiliaApiResponses
  @GetMapping("/popular")
  public ResponseEntity<List<Person>> getPopularMovies() {
    return ResponseEntity.ok().body(personService.getPopularPeople());
  }

  @Operation(
      summary = "Get person details",
      description = "Returns detailed information for a given person ID.")
  @CinephiliaApiResponses
  @GetMapping("/{person-id}")
  public ResponseEntity<Person> getPersonDetails(@PathVariable("person-id") Long personId) {
    return ResponseEntity.ok(personService.getPersonDetails(personId));
  }

  @Operation(
      summary = "Get person filmography",
      description = "Returns combined movie and TV credits for a given person.")
  @CinephiliaApiResponses
  @GetMapping("/{person-id}/credits")
  public ResponseEntity<List<Credit>> getCredits(@PathVariable("person-id") Long personId) {
    return ResponseEntity.ok().body(personService.getPersonFilmography(personId));
  }
}
