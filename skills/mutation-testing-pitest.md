# Skill: Configurar mutation testing com PIT

## Passos por módulo de negócio (pecas-service, clientes-service, representantes-service)
1. Adicionar o plugin org.pitest:pitest-maven no pom.xml do módulo
2. Configurar targetClasses apontando para o pacote base do serviço
   (ex: com.sistemapecas.pecas.*)
3. Configurar targetTests apontando para os testes existentes
4. Rodar: ./mvnw org.pitest:pitest-maven:mutationCoverage -pl {modulo}
5. O relatório HTML fica em target/pit-reports/ — abrir e revisar mutantes
   sobreviventes (survived mutants indicam lacunas nos testes)
6. Se o mutation score ficar baixo em alguma classe crítica (ex: Service com
   lógica de validação/duplicidade), adicionar testes extras para matar os
   mutantes sobreviventes, seguindo o padrão AAA do guia de testes
