package com.sistemapecas.pecas;

import com.sistemapecas.pecas.controller.PecaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de inicialização (smoke test) do Spring ApplicationContext para pecas-service.
 * Desativa busca obrigatória ao Config Server e Eureka para execução isolada.
 * Segue o padrão AAA e anotações descritivas conforme guia do arquiteto.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class PecasServiceApplicationTests {

    @Autowired
    private PecaController pecaController;

    @Test
    @DisplayName("Deve carregar o ApplicationContext e injetar o PecaController com sucesso")
    void contextLoads_quandoContextoInicializado_carregaBeansComSucesso() {
        // Arrange
        // O ApplicationContext é configurado e carregado pelo Spring Boot Test

        // Act
        // Os beans da aplicação são instanciados e injetados pelo Spring

        // Assert
        assertNotNull(pecaController, "O PecaController deve ser instanciado e injetado com sucesso no contexto");
    }
}
