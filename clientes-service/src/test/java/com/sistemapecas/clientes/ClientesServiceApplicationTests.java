package com.sistemapecas.clientes;

import com.sistemapecas.clientes.controller.ClienteController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de inicialização (smoke test) do Spring ApplicationContext para clientes-service.
 * Desativa busca obrigatória ao Config Server e Eureka para execução isolada.
 * Segue o padrão AAA e anotações descritivas conforme guia do arquiteto.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class ClientesServiceApplicationTests {

    @Autowired
    private ClienteController clienteController;

    @Test
    @DisplayName("Deve carregar o ApplicationContext e injetar o ClienteController com sucesso")
    void contextLoads_quandoContextoInicializado_carregaBeansComSucesso() {
        // Arrange
        // O ApplicationContext é configurado e carregado pelo Spring Boot Test

        // Act
        // Os beans da aplicação são instanciados e injetados pelo Spring

        // Assert
        assertNotNull(clienteController, "O ClienteController deve ser instanciado e injetado com sucesso no contexto");
    }
}
