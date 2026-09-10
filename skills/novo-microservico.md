# Skill: Criar novo microserviço de negócio (CRUD simples)

Use este skill quando precisar adicionar um novo microserviço seguindo o
mesmo padrão de pecas-service, clientes-service e representantes-service.

## Passos
1. Ler a estrutura de um serviço existente como referência (ex: pecas-service)
2. Criar entity/repository/service/controller/exception seguindo exatamente
   o mesmo padrão de camadas
3. Configurar application.yml para puxar config do config-server
   (spring.config.import: optional:configserver:http://localhost:8888)
4. Adicionar entrada correspondente em config-repo/{nome-servico}.yml com
   server.port e configuração do Eureka client
5. Escrever testes de controller cobrindo: criação com sucesso, validação de
   campo obrigatório, rejeição de duplicidade, busca por id/nome/identificador
   único, 404 em não encontrado
6. Verificar subindo config-server + eureka-server + o novo serviço, confirmar
   registro no Eureka, testar todos os endpoints via curl
7. Rodar ./mvnw clean test no projeto inteiro
8. Se o gateway já existir, adicionar a rota correspondente e re-testar via
   localhost:8080/api/{recurso}
