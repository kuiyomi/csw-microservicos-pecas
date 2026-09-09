package com.sistemapecas.pecas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste básico de carregamento de contexto do Spring Boot para pecas-service.
 * Desativa busca obrigatória ao Config Server e Eureka para execução isolada.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class PecasServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifica se todos os beans e configurações sobem com sucesso
    }
}
