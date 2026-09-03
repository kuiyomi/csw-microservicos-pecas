package com.sistemapecas.clientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Microserviço responsável pelo cadastro e gerenciamento de clientes.
 *
 * Anotações:
 * - @SpringBootApplication: Configuração do contexto e inicialização do Spring Boot.
 * - @EnableDiscoveryClient: Habilita o registro e descoberta no Eureka Server.
 * - @EnableFeignClients: Habilita clientes Feign para chamadas a outros microserviços.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClientesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientesServiceApplication.class, args);
    }
}
