# Skill: Verificação end-to-end do sistema completo

Use quando precisar confirmar que todo o sistema funciona integrado.

## Passos
1. Subir na ordem: config-server -> eureka-server -> pecas-service ->
   clientes-service -> representantes-service -> gateway
2. Confirmar todos os 4 serviços de aplicação registrados no Eureka
   (curl http://localhost:8761/eureka/apps)
3. Testar cada endpoint de cada recurso APENAS via gateway
   (localhost:8080/api/pecas, /api/clientes, /api/representantes) -
   nunca direto nas portas individuais, pois o requisito do projeto exige
   acesso exclusivo pelo Gateway
4. Testar uma rota inexistente e confirmar que retorna erro tratado, não crash
5. Parar todos os processos e confirmar portas liberadas
6. Rodar ./mvnw clean test no projeto inteiro
