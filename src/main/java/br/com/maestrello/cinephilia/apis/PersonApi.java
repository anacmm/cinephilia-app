package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.model.CreditResponse;
import br.com.maestrello.cinephilia.model.Person;
import br.com.maestrello.cinephilia.model.PersonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "person-api", url = "${tmdb.base-url}")
public interface PersonApi {

  @GetMapping("/person/popular")
  PersonResponse getPopularPeople();

  @GetMapping("/person/popular")
  PersonResponse getPopularPeople(@RequestParam("page") int page);

  @GetMapping("/person/{person-id}")
  Person getPerson(@PathVariable("person-id") Long personId);

  @GetMapping("/person/{person-id}/movie_credits")
  CreditResponse getMovieCredits(@PathVariable("person-id") Long personId);

  @GetMapping("/person/{person-id}/tv_credits")
  CreditResponse getTvCredits(@PathVariable("person-id") Long personId);
}
