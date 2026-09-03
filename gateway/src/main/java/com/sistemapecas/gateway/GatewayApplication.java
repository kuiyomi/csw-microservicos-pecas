package com.sistemapecas.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Ponto de entrada único (API Gateway) para as requisições externas do sistema.
 * Realiza o roteamento inteligente e balanceamento de carga para os microserviços.
 *
 * Anotações:
 * - @SpringBootApplication: Inicializa a aplicação Spring Boot (WebFlux / Reativa).
 * - @EnableDiscoveryClient: Habilita a descoberta dinâmica das rotas registradas no Eureka.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
