package br.com.maestrello.cinephilia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CinephiliaApplication {

  public static void main(String[] args) {
    SpringApplication.run(CinephiliaApplication.class, args);
  }
}
