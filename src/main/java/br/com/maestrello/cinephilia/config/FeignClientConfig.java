package br.com.maestrello.cinephilia.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEncryptableProperties
public class FeignClientConfig {

  public static final String API_KEY = "api_key";

  @Value("${tmdb.api-key}")
  private String apiKey;

  @Bean
  public RequestInterceptor requestInterceptor() {
    return template -> template.query(API_KEY, apiKey);
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}
