package br.com.maestrello.cinephilia.apis;

import br.com.maestrello.cinephilia.domain.Person;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "person-api", url = "${tmdb.base-url}")
public interface PersonApi {

    @GetMapping("/person/popular")
    List<Person> getPopularPeople();

}