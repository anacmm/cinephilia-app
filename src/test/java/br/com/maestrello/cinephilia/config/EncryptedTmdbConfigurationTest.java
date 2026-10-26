package br.com.maestrello.cinephilia.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.maestrello.cinephilia.service.GameService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.List;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@MockitoBean(types = GameService.class)
@SpringBootTest
@ActiveProfiles("test")
class EncryptedTmdbConfigurationTest {
  @Autowired private RequestInterceptor requestInterceptor;

  @DynamicPropertySource
  static void encryptedKey(DynamicPropertyRegistry registry) {
    registry.add("jasypt.encryptor.password", () -> "yourTestPassword");
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword("yourTestPassword");
    encryptor.setAlgorithm("PBEWithMD5AndDES");
    encryptor.setIvGenerator(new NoIvGenerator());
    String encrypted = encryptor.encrypt("test-only-api-key");
    registry.add("tmdb.api-key", () -> "ENC(" + encrypted + ")");
  }

  @Test
  void decryptsExistingAlgorithmAndSuppliesFeignAuthentication() {
    RequestTemplate template = new RequestTemplate();
    requestInterceptor.apply(template);
    assertEquals(List.of("test-only-api-key"), List.copyOf(template.queries().get("api_key")));
  }
}
