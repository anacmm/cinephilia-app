package br.com.maestrello.cinephilia;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.maestrello.cinephilia.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@MockitoBean(types = GameService.class)
@SpringBootTest
@ActiveProfiles("test")
class CinephiliaApplicationTests {

  @Autowired private ApplicationContext applicationContext;

  @Test
  void contextLoads() {
    assertNotNull(applicationContext);
  }
}
