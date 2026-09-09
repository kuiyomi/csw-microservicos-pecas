package com.sistemapecas.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Testes básicos de inicialização do contexto da aplicação API Gateway.
 * Desativa busca obrigatória ao Config Server e Eureka para execução isolada de testes.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
        // Valida se o contexto reativo do Spring Cloud Gateway inicializa com sucesso
    }
}
