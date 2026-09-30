package com.sistemapecas.pecas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.pecas.model.Peca;
import com.sistemapecas.pecas.repository.PecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints REST do pecas-service.
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
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        pecaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /pecas deve incrementar o contador pecas.cadastradas.total ao cadastrar peça")
    void cadastrar_pecaValida_deveIncrementarContadorMetrica() throws Exception {
        // Arrange
        double countInicial = meterRegistry.get("pecas.cadastradas.total").counter().count();
        Peca peca = new Peca("PEC-MET-01", "Pastilha Cerâmica", "Pastilha especial");

        // Act
        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peca)))
                .andExpect(status().isCreated());

        // Assert
        double countFinal = meterRegistry.get("pecas.cadastradas.total").counter().count();
        org.hamcrest.MatcherAssert.assertThat(countFinal, is(countInicial + 1.0));
    }

    @Test
    @DisplayName("POST /pecas deve cadastrar uma nova peça e retornar 201 Created")
    void cadastrarPecaComSucesso() throws Exception {
        Peca peca = new Peca("PEC-001", "Amortecedor Dianteiro", "Amortecedor a gás pressurizado");

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peca)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nroIdentificacao").value("PEC-001"))
                .andExpect(jsonPath("$.nome").value("Amortecedor Dianteiro"))
                .andExpect(jsonPath("$.descricao").value("Amortecedor a gás pressurizado"));
    }

    @Test
    @DisplayName("POST /pecas sem nroIdentificacao deve retornar 400 com mensagem de erro clara")
    void cadastrarPecaSemNroIdentificacaoDeveFalhar() throws Exception {
        Peca peca = new Peca(null, "Pastilha de Freio", "Pastilha de cerâmica");

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peca)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("número de identificação é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas sem nome deve retornar 400 com mensagem de erro clara")
    void cadastrarPecaSemNomeDeveFalhar() throws Exception {
        Peca peca = new Peca("PEC-002", null, "Pastilha de freio traseira");

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peca)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /pecas com nroIdentificacao duplicado deve ser rejeitado com 400")
    void cadastrarPecaComNroIdentificacaoDuplicadoDeveSerRejeitado() throws Exception {
        Peca peca1 = new Peca("PEC-DUP", "Filtro de Óleo", "Filtro blindado");
        pecaRepository.save(peca1);

        Peca peca2 = new Peca("PEC-DUP", "Outro Filtro", "Descrição alternativa");

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peca2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Já existe uma peça cadastrada com o número de identificação")));
    }

    @Test
    @DisplayName("GET /pecas deve retornar lista de todas as peças")
    void listarTodasPecas() throws Exception {
        pecaRepository.save(new Peca("PEC-10", "Vela de Ignição", "Vela Iridium"));
        pecaRepository.save(new Peca("PEC-20", "Correia Dentada", "Correia de alta resistência"));

        mockMvc.perform(get("/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nroIdentificacao").value("PEC-10"))
                .andExpect(jsonPath("$[1].nroIdentificacao").value("PEC-20"));
    }

    @Test
    @DisplayName("GET /pecas/{id} deve retornar a peça quando encontrada")
    void buscarPorIdExistente() throws Exception {
        Peca salva = pecaRepository.save(new Peca("PEC-30", "Disco de Freio", "Disco ventilado"));

        mockMvc.perform(get("/pecas/{id}", salva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salva.getId()))
                .andExpect(jsonPath("$.nroIdentificacao").value("PEC-30"))
                .andExpect(jsonPath("$.nome").value("Disco de Freio"));
    }

    @Test
    @DisplayName("GET /pecas/{id} deve retornar 404 quando não encontrada")
    void buscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/pecas/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Peça não encontrada com o ID: 9999")));
    }

    @Test
    @DisplayName("GET /pecas/nome/{nome} deve realizar busca parcial case-insensitive")
    void buscarPorNomeCaseInsensitivePartialMatch() throws Exception {
        pecaRepository.save(new Peca("P-01", "Bateria Automotiva 60Ah", "Bateria selada"));
        pecaRepository.save(new Peca("P-02", "Cabo para BATERIA", "Cabos de alta condutividade"));
        pecaRepository.save(new Peca("P-03", "Radiador", "Radiador de alumínio"));

        // Busca com termo em minúsculo "bateria"
        mockMvc.perform(get("/pecas/nome/bateria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nroIdentificacao", containsInAnyOrder("P-01", "P-02")));

        // Busca com parte do termo "auto"
        mockMvc.perform(get("/pecas/nome/AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nroIdentificacao").value("P-01"));
    }

    @Test
    @DisplayName("GET /pecas/identificacao/{nroIdentificacao} deve retornar a peça se exata")
    void buscarPorIdentificacaoExistente() throws Exception {
        pecaRepository.save(new Peca("COD-EXATO-123", "Bomba de Combustível", "Bomba elétrica"));

        mockMvc.perform(get("/pecas/identificacao/COD-EXATO-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nroIdentificacao").value("COD-EXATO-123"))
                .andExpect(jsonPath("$.nome").value("Bomba de Combustível"));
    }

    @Test
    @DisplayName("GET /pecas/identificacao/{nroIdentificacao} deve retornar 404 se não encontrada")
    void buscarPorIdentificacaoInexistente() throws Exception {
        mockMvc.perform(get("/pecas/identificacao/COD-INEXISTENTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Peça não encontrada com o número de identificação: COD-INEXISTENTE")));
    }
}
