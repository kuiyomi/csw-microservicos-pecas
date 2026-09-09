# Sistema de Microserviços para Gestão de Peças Automotivas

Projeto multi-módulo construído com **Java 17**, **Spring Boot 3.2.x**, **Spring Cloud 2023.x** e banco de dados em memória **H2**. O sistema adota padrões consolidados de arquitetura distribuída: configuração centralizada (*Externalized Configuration*), descoberta dinâmica de serviços (*Service Discovery & Registry*) e roteamento unificado via borda (*API Gateway*).

---

## 1. Arquitetura do Sistema

O sistema é composto por 6 módulos independentes divididos entre infraestrutura e serviços de negócio:

```
                          +-----------------------------+
                          |   Cliente HTTP / Frontend   |
                          +--------------+--------------+
                                         |
                                         | Requisições na porta 8080: /api/...
                                         v
                      +------------------------------------+
                      |    API Gateway (Spring Cloud)      |
                      |            (Porta 8080)            |
                      +-----+--------------+----------+----+
                            |              |          |
            lb://pecas-service   lb://clientes-service lb://representantes-service
                            |              |          |
        +-------------------+              |          +-------------------+
        v                                  v                              v
+------------------+             +-------------------+          +-------------------+
|  pecas-service   |             | clientes-service  |          |representantes-srv |
|   (Porta 8081)   |             |   (Porta 8082)    |          |   (Porta 8083)    |
|  [H2 in-memory]  |             |  [H2 in-memory]   |          |  [H2 in-memory]   |
+--------+---------+             +---------+---------+          +---------+---------+
         |                                 |                              |
         +--------------------+            |            +-----------------+
                              |            |            |
                              v            v            v
                      +------------------------------------+
                      |    Eureka Server (Service Registry)|
                      |            (Porta 8761)            |
                      +--------------------+---------------+
                                           ^
                                           | Obtém configurações centralizadas
                      +--------------------+---------------+
                      |   Config Server (Spring Cloud)     |
                      |            (Porta 8888)            |
                      +--------------------+---------------+
                                           | Lê arquivos .yml
                                           v
                                   [config-repo/]
```

### Detalhamento dos Módulos

1. **`config-server` (Porta 8888)**
   - Servidor central de configurações baseado no **Spring Cloud Config Server**.
   - Utiliza o perfil nativo (`spring.profiles.active=native`) apontando para o diretório local `config-repo/`.
   - Fornece de forma dinâmica e centralizada as configurações para todos os outros componentes da arquitetura.

2. **`eureka-server` (Porta 8761)**
   - Servidor de descoberta e registro de serviços baseado no **Netflix Eureka**.
   - Mantém o catálogo atualizado de instâncias ativas de cada microserviço e monitora seus batimentos cardíacos (*heartbeats*).
   - Disponibiliza dashboard web de monitoramento em `http://localhost:8761`.

3. **`gateway` (Porta 8080)**
   - Ponto de entrada unificado da aplicação construído com **Spring Cloud Gateway** (reativo / WebFlux).
   - Integra-se ao Eureka Server para realizar descoberta dinâmica e balanceamento de carga (`lb://pecas-service`, `lb://clientes-service`, `lb://representantes-service`).
   - Mapeia o prefixo público `/api/{servico}/**` e aplica o filtro `StripPrefix=1` para repassar o caminho correspondente diretamente ao respectivo microserviço (`/pecas/**`, `/clientes/**`, `/representantes/**`).
   - Retorna respostas de erro estruturadas (ex: `404 Not Found`) para rotas inexistentes.

4. **`pecas-service` (Porta 8081)**
   - Microserviço de negócio responsável pelo catálogo e gestão de peças automotivas.
   - Permite o cadastro, listagem geral, consulta por identificador numérico (ID), filtro por nome e busca exata por número de identificação único.
   - Utiliza banco de dados relacional em memória H2 com Spring Data JPA.

5. **`clientes-service` (Porta 8082)**
   - Microserviço de negócio responsável pela gestão de clientes.
   - Fornece cadastro com validação de CPF único, listagem, consulta por ID, filtro por nome e busca direta por CPF.
   - Utiliza banco de dados relacional em memória H2 com Spring Data JPA.

6. **`representantes-service` (Porta 8083)**
   - Microserviço de negócio responsável pela gestão de representantes comerciais.
   - Fornece cadastro com validação de CPF único, listagem, consulta por ID, filtro por nome e busca direta por CPF.
   - Utiliza banco de dados relacional em memória H2 com Spring Data JPA.

---

## 2. Ordem de Inicialização e Comandos

### Por que a ordem é fundamental?
1. **1º `config-server`**: Deve iniciar primeiro pois todos os outros serviços dependem dele para carregar suas portas, conexões com banco e regras de registro.
2. **2º `eureka-server`**: Inicializa em segundo lugar para estar pronto para receber os registros dos microserviços de negócio e do gateway.
3. **3º `pecas-service`, `clientes-service` e `representantes-service`**: Devem ser iniciados para se registrar no Eureka Server.
4. **4º `gateway`**: Inicia por último (ou após o Eureka estar acessível), descobrindo os microserviços registrados no Eureka e habilitando o roteamento dinâmico.

---

### Execução via Maven (`./mvnw`)

Abra terminais separados para cada módulo ou execute-os em segundo plano:

```bash
# 1. Iniciar o Servidor de Configuração (Porta 8888)
./mvnw spring-boot:run -pl config-server

# 2. Iniciar o Eureka Server (Porta 8761)
./mvnw spring-boot:run -pl eureka-server

# 3. Iniciar o Microserviço de Peças (Porta 8081)
./mvnw spring-boot:run -pl pecas-service

# 4. Iniciar o Microserviço de Clientes (Porta 8082)
./mvnw spring-boot:run -pl clientes-service

# 5. Iniciar o Microserviço de Representantes (Porta 8083)
./mvnw spring-boot:run -pl representantes-service

# 6. Iniciar o API Gateway (Porta 8080)
./mvnw spring-boot:run -pl gateway
```

---

### Execução via JARs Empacotados

Caso prefira compilar os artefatos executáveis e executá-los diretamente com o runtime do Java:

```bash
# Empacotar todos os módulos sem rodar testes
./mvnw package -DskipTests

# 1. Config Server
java -jar config-server/target/config-server-1.0.0-SNAPSHOT.jar

# 2. Eureka Server
java -jar eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar

# 3. Peças Service
java -jar pecas-service/target/pecas-service-1.0.0-SNAPSHOT.jar

# 4. Clientes Service
java -jar clientes-service/target/clientes-service-1.0.0-SNAPSHOT.jar

# 5. Representantes Service
java -jar representantes-service/target/representantes-service-1.0.0-SNAPSHOT.jar

# 6. Gateway
java -jar gateway/target/gateway-1.0.0-SNAPSHOT.jar
```

---

### Verificação de Saúde dos Serviços

| Serviço | Endpoint de Health Check | Resposta Esperada |
| :--- | :--- | :--- |
| **Config Server** | `http://localhost:8888/actuator/health` | `{"status":"UP"}` |
| **Eureka Server** | `http://localhost:8761/actuator/health` | `{"status":"UP"}` |
| **Dashboard Eureka** | `http://localhost:8761/` | Painel web com as instâncias listadas |
| **Peças Service** | `http://localhost:8081/actuator/health` | `{"status":"UP"}` |
| **Clientes Service** | `http://localhost:8082/actuator/health` | `{"status":"UP"}` |
| **Representantes Service** | `http://localhost:8083/actuator/health` | `{"status":"UP"}` |
| **API Gateway** | `http://localhost:8080/actuator/health` | `{"status":"UP"}` |

---

## 3. Tabela de Endpoints da API (via Gateway)

Todas as requisições externas devem ser direcionadas ao **API Gateway** na porta `8080`. O gateway reescreve a URL removendo `/api` e repassa a requisição ao microserviço correspondente registrado no Eureka.

### 3.1. Peças (`pecas-service`)

| Método | Endpoint via Gateway | Serviço Destino | Descrição | Status Sucesso | Status Erro |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/pecas` | `pecas-service` | Cadastra uma nova peça no catálogo | `201 Created` | `400 Bad Request` |
| `GET` | `/api/pecas` | `pecas-service` | Lista todas as peças cadastradas | `200 OK` | - |
| `GET` | `/api/pecas/{id}` | `pecas-service` | Busca uma peça específica pelo ID | `200 OK` | `404 Not Found` |
| `GET` | `/api/pecas/nome/{nome}` | `pecas-service` | Filtra peças por nome parcial (*case-insensitive*) | `200 OK` | - |
| `GET` | `/api/pecas/identificacao/{nro}` | `pecas-service` | Busca uma peça pelo número de identificação único | `200 OK` | `404 Not Found` |

#### Exemplo de Payload para Cadastro de Peça:
```json
{
  "nroIdentificacao": "PEC-001",
  "nome": "Pastilha de Freio Dianteira",
  "descricao": "Pastilha de freio cerâmica para veículos leves"
}
```

---

### 3.2. Clientes (`clientes-service`)

| Método | Endpoint via Gateway | Serviço Destino | Descrição | Status Sucesso | Status Erro |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/clientes` | `clientes-service` | Cadastra um novo cliente | `201 Created` | `400 Bad Request` |
| `GET` | `/api/clientes` | `clientes-service` | Lista todos os clientes cadastrados | `200 OK` | - |
| `GET` | `/api/clientes/{id}` | `clientes-service` | Busca um cliente específico pelo ID | `200 OK` | `404 Not Found` |
| `GET` | `/api/clientes/nome/{nome}` | `clientes-service` | Filtra clientes por nome parcial (*case-insensitive*) | `200 OK` | - |
| `GET` | `/api/clientes/cpf/{cpf}` | `clientes-service` | Busca um cliente pelo CPF único | `200 OK` | `404 Not Found` |

#### Exemplo de Payload para Cadastro de Cliente:
```json
{
  "cpf": "12345678901",
  "nome": "Carlos Eduardo Silva"
}
```

---

### 3.3. Representantes Comerciais (`representantes-service`)

| Método | Endpoint via Gateway | Serviço Destino | Descrição | Status Sucesso | Status Erro |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/representantes` | `representantes-service` | Cadastra um novo representante comercial | `201 Created` | `400 Bad Request` |
| `GET` | `/api/representantes` | `representantes-service` | Lista todos os representantes cadastrados | `200 OK` | - |
| `GET` | `/api/representantes/{id}` | `representantes-service` | Busca um representante específico pelo ID | `200 OK` | `404 Not Found` |
| `GET` | `/api/representantes/nome/{nome}` | `representantes-service` | Filtra representantes por nome parcial (*case-insensitive*) | `200 OK` | - |
| `GET` | `/api/representantes/cpf/{cpf}` | `representantes-service` | Busca um representante pelo CPF único | `200 OK` | `404 Not Found` |

#### Exemplo de Payload para Cadastro de Representante:
```json
{
  "cpf": "98765432100",
  "nome": "Ana Beatriz Santos"
}
```

---

### 3.4. Tratamento de Rotas Inexistentes no Gateway

Caso uma requisição seja enviada a um caminho não mapeado no Gateway:
- **Exemplo**: `GET http://localhost:8080/api/rota-inexistente` ou `GET http://localhost:8080/desconhecido`
- **Status Retornado**: `404 Not Found`
- **Corpo da Resposta**:
  ```json
  {
    "timestamp": "2026-09-09T00:59:13.201+00:00",
    "path": "/api/rota-inexistente",
    "status": 404,
    "error": "Not Found",
    "requestId": "02a8945d-14"
  }
  ```

---

## 4. Testes Automatizados

O projeto conta com suíte completa de testes de contexto e testes de integração com MockMvc para os controladores REST.

Para executar todos os testes automatizados do projeto multi-módulo:

```bash
./mvnw clean test
```

---

## 5. Coleções para Teste Manual das Requisições

Para facilitar os testes de integração em ferramentas como **Postman**, **Insomnia**, **VS Code REST Client** ou **IntelliJ HTTP Client**, o repositório disponibiliza dois arquivos na raiz:

1. **`requests.http`**: Arquivo no formato padrão HTTP Client com todos os cenários prontos para execução com um clique no VS Code (extensão REST Client) ou IntelliJ IDEA.
2. **`sistema-pecas.postman_collection.json`**: Coleção oficial do Postman (formato v2.1) pronta para importação direta no Postman.

Ambos os arquivos já vêm pré-configurados utilizando a variável `baseUrl = http://localhost:8080` e cobrem todas as operações de cadastro, listagem, buscas por filtros, validações de erro e testes de rotas inexistentes.
