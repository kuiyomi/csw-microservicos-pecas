package com.sistemapecas.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Servidor centralizado de configurações para todos os microserviços do sistema.
 *
 * Anotações:
 * - @SpringBootApplication: Inicializa a aplicação Spring Boot.
 * - @EnableConfigServer: Habilita os recursos do Spring Cloud Config Server.
 * - @EnableDiscoveryClient: Registra a instância no serviço de descoberta (Eureka).
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
