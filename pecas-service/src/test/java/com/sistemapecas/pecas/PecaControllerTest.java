package com.sistemapecas.pecas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.pecas.model.Peca;
import com.sistemapecas.pecas.repository.PecaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints REST do pecas-service.
 *
 * Práticas e convenções adotadas (conforme testes__2_.pdf):
 *  - Padrão AAA estrito: comentários explícitos // Arrange, // Act, // Assert em todos os métodos.
 *  - Separação entre a execução da requisição (Act) e verificação dos resultados (Assert).
 *  - Convenção descritiva: metodoTestado_cenario_comportamentoEsperado.
 *  - @DisplayName descritivo em português em 100% dos testes.
 *  - Testes de casos limite (edge cases): valores nulos vs string vazia, campos somente com espaços/tabulações,
 *    aparamento (trim) de espaços, e busca parcial com sensibilidade de caso (case-insensitive com mixed-case).
 *  - Verificação de métricas baseada em delta relativo (+1.0 ou +2.0), evitando dependência de valores absolutos
 *    já que o MeterRegistry é estado compartilhado entre os testes.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
@AutoConfigureMockMvc
class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PecaRepository pecaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        pecaRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // POST /pecas — cadastro e validações
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /pecas com dados válidos deve retornar 201 Created com o corpo da peça cadastrada")
    void cadastrar_pecaValida_retorna201ComCorpo() throws Exception {
        // Arrange
        Peca peca = new Peca("PEC-001", "Amortecedor Dianteiro", "Amortecedor a gás pressurizado");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nroIdentificacao").value("PEC-001"))
                .andExpect(jsonPath("$.nome").value("Amortecedor Dianteiro"))
                .andExpect(jsonPath("$.descricao").value("Amortecedor a gás pressurizado"));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao nulo deve retornar 400 Bad Request")
    void cadastrar_nroIdentificacaoNulo_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca(null, "Pastilha de Freio", "Pastilha de cerâmica");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("número de identificação é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_nroIdentificacaoVazio_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("", "Pastilha de Freio", "Pastilha de cerâmica");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("número de identificação é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao contendo apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_nroIdentificacaoSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("   ", "Pastilha de Freio", "Pastilha de cerâmica");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("número de identificação é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao com tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_nroIdentificacaoComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca(" \t \n ", "Pastilha de Freio", "Pastilha de cerâmica");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("número de identificação é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nome nulo deve retornar 400 Bad Request")
    void cadastrar_nomeNulo_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("PEC-002", null, "Pastilha de freio traseira");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nome vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_nomeVazio_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("PEC-003", "", "Pastilha de freio traseira");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nome contendo apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_nomeSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("PEC-004", "   ", "Pastilha de freio traseira");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nome contendo tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_nomeComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("PEC-005", " \t \n ", "Pastilha de freio traseira");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com ambos os campos obrigatórios nulos deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosNulos_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca(null, null, "Descrição sem identificador");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /pecas com ambos os campos obrigatórios vazios deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosVazios_retorna400() throws Exception {
        // Arrange
        Peca peca = new Peca("", "", "Descrição sem identificador");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /pecas com espaços excedentes nas extremidades deve aparar os campos e salvar com sucesso")
    void cadastrar_camposComEspacosNasExtremidades_salvaComCamposAparados() throws Exception {
        // Arrange
        Peca peca = new Peca("  PEC-TRIM-01  ", "  Filtro de Óleo Especial  ", "Filtro blindado");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.nroIdentificacao").value("PEC-TRIM-01"))
                .andExpect(jsonPath("$.nome").value("Filtro de Óleo Especial"));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao duplicado deve retornar 400 Bad Request indicando conflito")
    void cadastrar_nroIdentificacaoDuplicado_retorna400ComMensagemConflito() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("PEC-DUP", "Filtro de Óleo", "Filtro blindado"));
        Peca peca2 = new Peca("PEC-DUP", "Outro Filtro", "Descrição alternativa");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca2)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Já existe uma peça cadastrada com o número de identificação")));
    }

    // -------------------------------------------------------------------------
    // Métricas — Micrometer Counter (pecas.cadastradas.total)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /pecas com sucesso deve incrementar o contador pecas.cadastradas.total por delta (+1.0)")
    void cadastrar_pecaValida_incrementaContadorMetricaEmUm() throws Exception {
        // Arrange
        // Lê o valor atual do contador sem assumir valor zero absoluto, pois
        // o MeterRegistry é compartilhado entre testes e outros testes já podem ter executado
        double countAntes = meterRegistry.get("pecas.cadastradas.total").counter().count();
        Peca peca = new Peca("PEC-MET-01", "Pastilha Cerâmica", "Pastilha especial");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peca)));

        // Assert
        resultado.andExpect(status().isCreated());
        double countDepois = meterRegistry.get("pecas.cadastradas.total").counter().count();
        assertThat("O contador deve incrementar exatamente 1 unidade em relação ao valor anterior",
                countDepois, is(countAntes + 1.0));
    }

    @Test
    @DisplayName("POST /pecas com dados inválidos não deve incrementar o contador de métricas")
    void cadastrar_pecaInvalida_naoIncrementaContadorMetrica() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("pecas.cadastradas.total").counter().count();
        Peca pecaInvalida = new Peca("", "", "Incompleto");

        // Act
        ResultActions resultado = mockMvc.perform(post("/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pecaInvalida)));

        // Assert
        resultado.andExpect(status().isBadRequest());
        double countDepois = meterRegistry.get("pecas.cadastradas.total").counter().count();
        assertThat("O contador de métricas não deve sofrer alteração quando a validação falhar",
                countDepois, is(countAntes));
    }

    @Test
    @DisplayName("POST /pecas múltiplos cadastros válidos devem incrementar contador por delta relativo sem depender de valor absoluto")
    void cadastrar_multiplosCadastrosValidos_incrementaContadorPorDeltaRelativo() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("pecas.cadastradas.total").counter().count();
        Peca pecaA = new Peca("PEC-DELTA-01", "Peça Delta A", "Descrição Delta");
        Peca pecaB = new Peca("PEC-DELTA-02", "Peça Delta B", "Descrição Delta");

        // Act
        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pecaA)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pecaB)))
                .andExpect(status().isCreated());

        // Assert
        double countDepois = meterRegistry.get("pecas.cadastradas.total").counter().count();
        assertThat("O contador deve incrementar exatamente 2 unidades acima do valor pré-existente",
                countDepois, is(countAntes + 2.0));
    }

    // -------------------------------------------------------------------------
    // GET /pecas — listagem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /pecas deve retornar lista com todas as peças cadastradas")
    void listarTodas_comDuasPecas_retornaListaComDoisItens() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("PEC-10", "Vela de Ignição", "Vela Iridium"));
        pecaRepository.save(new Peca("PEC-20", "Correia Dentada", "Correia de alta resistência"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nroIdentificacao").value("PEC-10"))
                .andExpect(jsonPath("$[1].nroIdentificacao").value("PEC-20"));
    }

    // -------------------------------------------------------------------------
    // GET /pecas/{id} — busca por ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /pecas/{id} com ID existente deve retornar a peça correspondente")
    void buscarPorId_idExistente_retornaPecaCorreta() throws Exception {
        // Arrange
        Peca salva = pecaRepository.save(new Peca("PEC-30", "Disco de Freio", "Disco ventilado"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/{id}", salva.getId()));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salva.getId()))
                .andExpect(jsonPath("$.nroIdentificacao").value("PEC-30"))
                .andExpect(jsonPath("$.nome").value("Disco de Freio"));
    }

    @Test
    @DisplayName("GET /pecas/{id} com ID inexistente deve retornar 404 Not Found")
    void buscarPorId_idInexistente_retorna404() throws Exception {
        // Arrange — banco de dados garantidamente limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/9999"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Peça não encontrada com o ID: 9999")));
    }

    // -------------------------------------------------------------------------
    // GET /pecas/nome/{nome} — busca parcial case-insensitive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /pecas/nome/{nome} com termo em minúsculas deve encontrar peças (case-insensitive)")
    void buscarPorNome_termoEmMinusculas_retornaPecasQueContemOTermo() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva 60Ah", "Bateria selada"));
        pecaRepository.save(new Peca("P-02", "Cabo para BATERIA", "Cabos de alta condutividade"));
        pecaRepository.save(new Peca("P-03", "Radiador", "Radiador de alumínio"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/nome/bateria"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nroIdentificacao", containsInAnyOrder("P-01", "P-02")));
    }

    @Test
    @DisplayName("GET /pecas/nome/{nome} com termo em maiúsculas deve encontrar peças (case-insensitive)")
    void buscarPorNome_termoEmMaiusculas_retornaPecasQueContemOTermo() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva 60Ah", "Bateria selada"));
        pecaRepository.save(new Peca("P-02", "Cabo para BATERIA", "Cabos de alta condutividade"));
        pecaRepository.save(new Peca("P-03", "Radiador", "Radiador de alumínio"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/nome/AUTO"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nroIdentificacao").value("P-01"));
    }

    @Test
    @DisplayName("GET /pecas/nome/{nome} com termo em mixed-case alternado deve encontrar peças ignorando sensibilidade de caixa")
    void buscarPorNome_termoEmMixedCase_retornaPecasQueContemOTermo() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva 60Ah", "Bateria selada"));
        pecaRepository.save(new Peca("P-02", "Cabo para BATERIA", "Cabos de alta condutividade"));
        pecaRepository.save(new Peca("P-03", "Radiador", "Radiador de alumínio"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/nome/bAtErIa"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nroIdentificacao", containsInAnyOrder("P-01", "P-02")));
    }

    @Test
    @DisplayName("GET /pecas/nome/{nome} com substring no meio em mixed-case deve encontrar peças correspondentes")
    void buscarPorNome_substringMeioEmMixedCase_retornaPecasCorrespondentes() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva 60Ah", "Bateria selada"));
        pecaRepository.save(new Peca("P-02", "Radiador", "Radiador de alumínio"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/nome/tOmOtIv"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nroIdentificacao").value("P-01"));
    }

    @Test
    @DisplayName("GET /pecas/nome/{nome} com termo inexistente deve retornar lista vazia")
    void buscarPorNome_termoInexistente_retornaListaVazia() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva", "Bateria selada"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/nome/ROLAMENTO"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // GET /pecas/identificacao/{nroIdentificacao} — busca exata
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /pecas/identificacao/{nroIdentificacao} com código exato deve retornar a peça correspondente")
    void buscarPorNroIdentificacao_codigoExistente_retornaPecaCorreta() throws Exception {
        // Arrange
        pecaRepository.save(new Peca("COD-EXATO-123", "Bomba de Combustível", "Bomba elétrica"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/identificacao/COD-EXATO-123"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.nroIdentificacao").value("COD-EXATO-123"))
                .andExpect(jsonPath("$.nome").value("Bomba de Combustível"));
    }

    @Test
    @DisplayName("GET /pecas/identificacao/{nroIdentificacao} com código inexistente deve retornar 404 Not Found")
    void buscarPorNroIdentificacao_codigoInexistente_retorna404() throws Exception {
        // Arrange — banco de dados limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/pecas/identificacao/COD-INEXISTENTE"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        containsString("Peça não encontrada com o número de identificação: COD-INEXISTENTE")));
    }
}
