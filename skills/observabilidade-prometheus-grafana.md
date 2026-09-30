# Skill: Instrumentar microserviço com Prometheus + Grafana

## Passos por microserviço (pecas, clientes, representantes, gateway)
1. Adicionar dependências no pom.xml: spring-boot-starter-actuator e
   io.micrometer:micrometer-registry-prometheus
2. No application.yml do serviço (ou no config-repo, já que usamos config
   centralizada): expor management.endpoints.web.exposure.include=prometheus,health,info
3. Adicionar management.metrics.tags.application=${spring.application.name}
   para diferenciar as métricas de cada serviço no Prometheus
4. Nos serviços de negócio, criar um Counter customizado no Service para a
   operação de cadastro (ex: pecas.cadastradas.total), incrementado a cada
   POST bem-sucedido
5. Verificar: subir o serviço e curl localhost:{porta}/actuator/prometheus,
   confirmar que a métrica customizada aparece

## Containers
1. Criar docker-compose.yml na raiz com serviços prometheus e grafana
2. Criar prometheus/prometheus.yml com scrape_configs apontando para cada
   serviço nas portas 8080-8083 (no host, via host.docker.internal ou rede
   compartilhada, já que os serviços Spring Boot rodam localmente, não em
   container)
3. Criar grafana/provisioning/datasources com datasource do Prometheus
   provisionado automaticamente
4. Verificar: docker-compose up -d, acessar localhost:9090 (Prometheus) e
   confirmar os targets como UP, acessar localhost:3000 (Grafana) e confirmar
   o datasource já configurado
