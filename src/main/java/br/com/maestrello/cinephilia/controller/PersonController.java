package br.com.maestrello.cinephilia.controller;

import br.com.maestrello.cinephilia.model.Credit;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping("/popular")
    public ResponseEntity<List<Person>> getPopularMovies() {
        return ResponseEntity.ok().body(personService.getPopularPeople());
    }

    @GetMapping("/{person-id}")
    public Person getPersonDetails(@PathVariable("person-id") Long personId) {
        return personService.getPersonDetails(personId);
    }

    @GetMapping("/{person-id}/credits")
    public ResponseEntity<List<Credit>> getCredits(@PathVariable("person-id") Long personId) {
        return ResponseEntity.ok().body(personService.getPersonFilmography(personId));
    }

}