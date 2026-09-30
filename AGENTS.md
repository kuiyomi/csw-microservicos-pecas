cat > AGENTS.md << 'EOF'
# Sistema de Microserviços - Peças, Clientes e Representantes

## Contexto
Projeto acadêmico de arquitetura de software (microserviços + Spring Cloud).
Grupo de até 4 pessoas, desenvolvimento assistido por IA (Antigravity CLI).

## Stack
- Java 17, Spring Boot 3.2.5, Spring Cloud 2023.0.3
- Maven multi-módulo (mvnw incluído, sempre usar ./mvnw, não mvn global)
- H2 in-memory para todos os serviços de negócio
- JUnit + MockMvc para testes de controller

## Arquitetura e portas
- config-server (8888): Spring Cloud Config, profile "native", serve de config-repo/
- eureka-server (8761): standalone, register-with-eureka=false, fetch-registry=false
- gateway (8080): Spring Cloud Gateway, roteia /api/{recurso}/** -> lb://{recurso}-service
- pecas-service (8081)
- clientes-service (8082)
- representantes-service (8083)

## Ordem de inicialização (sempre)
config-server -> eureka-server -> serviços de negócio -> gateway

## Padrão arquitetural de cada microserviço de negócio
Sempre seguir a mesma estrutura em camadas (ver pecas-service como referência):
- model/ (entidade JPA)
- repository/ (Spring Data JPA)
- service/ (regras de negócio, validação de duplicidade)
- controller/ (REST endpoints)
- exception/ (ResourceNotFoundException, DuplicateXxxException, GlobalExceptionHandler)

## Convenções de API
- POST cadastrar -> 201, corpo com objeto criado
- GET listar -> 200
- GET por id -> 200 ou 404
- GET por busca textual (nome) -> case-insensitive, partial match
- GET por identificador único (cpf/nroIdentificacao) -> exact match, 404 se não existir
- Campo obrigatório ausente -> 400 com mensagem clara do campo
- Duplicidade de identificador único -> 400 com mensagem explicando o conflito

## Testes
Toda funcionalidade nova precisa de testes de controller (MockMvc) cobrindo:
sucesso, campo obrigatório ausente, duplicidade, não encontrado.

## Verificação obrigatória antes de considerar uma tarefa concluída
1. ./mvnw clean test deve passar no projeto inteiro
2. Se envolver serviço rodando: subir na ordem correta, checar registro no Eureka
   (curl http://localhost:8761/eureka/apps), testar endpoints via curl, parar tudo
   e confirmar portas liberadas (lsof)
3. Se envolver o gateway: testar sempre via localhost:8080/api/..., nunca direto
   na porta do serviço, já que o requisito do projeto exige acesso só pelo Gateway
EOF
## Observabilidade (fase 2 do projeto)
- Todo microserviço de negócio (pecas, clientes, representantes) e o gateway
  devem expor métricas via Spring Boot Actuator + Micrometer no formato
  Prometheus (endpoint /actuator/prometheus).
- management.endpoints.web.exposure.include deve incluir: prometheus,health,info
- Prometheus e Grafana rodam como containers Docker (docker-compose.yml na raiz).
- Prometheus faz scrape de cada serviço via sua porta individual (não via gateway).
- Métricas customizadas (Counter) devem ser criadas para as operações de
  cadastro em cada serviço (ex: pecas.cadastradas.total, clientes.cadastrados.total).
- Grafana deve ter o Prometheus provisionado como datasource automaticamente
  (não manualmente pela UI) via arquivo de provisionamento.

## Testes e qualidade (fase 3 do projeto)
- Seguir sempre o padrão Arrange-Act-Assert (AAA) nos testes, com comentários
  // Arrange, // Act, // Assert.
- Usar @DisplayName com nomes descritivos em português.
- Convenção de nome de método: metodoTestado_cenario_comportamentoEsperado
- Testes de controller: já usamos MockMvc (ver ClienteControllerTest como referência)
- Mutation testing via PIT (org.pitest:pitest-maven) em cada módulo de negócio,
  configurado no pom.xml de cada serviço. Meta de mutation coverage: reportar
  o que for atingido, sem necessidade de 100%.
