package com.sistemapecas.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de Descoberta (Service Registry) baseado em Netflix Eureka.
 * Permite que todas as instâncias de microserviços se registrem e descubram
 * dinamicamente a localização umas das outras.
 *
 * Anotações:
 * - @SpringBootApplication: Inicializa a aplicação Spring Boot.
 * - @EnableEurekaServer: Ativa o modo de servidor de registros Eureka.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
