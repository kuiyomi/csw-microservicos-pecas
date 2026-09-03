package com.sistemapecas.eurekaserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Testes basicos de contexto para o EurekaServerApplication.
 * Desativa a busca obrigatoria ao Config Server durante o teste unitario.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
        // Valida se o contexto da aplicacao sobe sem falhas
    }
}
