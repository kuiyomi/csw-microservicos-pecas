package com.sistemapecas.representantes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.representantes.model.Representante;
import com.sistemapecas.representantes.repository.RepresentanteRepository;
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
 * Testes de integração para os endpoints REST do representantes-service.
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
class RepresentanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepresentanteRepository representanteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        representanteRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // POST /representantes — cadastro e validações
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /representantes com dados válidos deve retornar 201 Created com o corpo do representante")
    void cadastrar_representanteValido_retorna201ComCorpo() throws Exception {
        // Arrange
        Representante rep = new Representante("12345678901", "Roberto Dias");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Roberto Dias"));
    }

    @Test
    @DisplayName("POST /representantes com CPF nulo deve retornar 400 Bad Request")
    void cadastrar_cpfNulo_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante(null, "Juliana Mendes");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com CPF vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_cpfVazio_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("", "Juliana Mendes");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com CPF apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_cpfSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("   ", "Juliana Mendes");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com CPF contendo tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_cpfComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante(" \t \n ", "Juliana Mendes");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com nome nulo deve retornar 400 Bad Request")
    void cadastrar_nomeNulo_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("98765432100", null);

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com nome vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_nomeVazio_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("98765432100", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com nome apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_nomeSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("98765432100", "   ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com nome contendo tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_nomeComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("98765432100", " \t \n ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com ambos os campos obrigatórios nulos deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosNulos_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante(null, null);

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /representantes com ambos os campos obrigatórios vazios deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosVazios_retorna400() throws Exception {
        // Arrange
        Representante rep = new Representante("", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /representantes com espaços excedentes nas extremidades deve aparar os campos e salvar com sucesso")
    void cadastrar_camposComEspacosNasExtremidades_salvaComCamposAparados() throws Exception {
        // Arrange
        Representante rep = new Representante("  12345678901  ", "  Roberto Dias Junior  ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Roberto Dias Junior"));
    }

    @Test
    @DisplayName("POST /representantes com CPF duplicado deve retornar 400 Bad Request indicando conflito")
    void cadastrar_cpfDuplicado_retorna400ComMensagemConflito() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("11122233344", "Lucas Almeida"));
        Representante rep2 = new Representante("11122233344", "Lucas Outro");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep2)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Já existe um representante cadastrado com o CPF")));
    }

    @Test
    @DisplayName("POST /representantes com corpo contendo JSON malformado deve retornar 400 Bad Request")
    void cadastrar_jsonMalformado_retorna400() throws Exception {
        // Arrange
        String jsonInvalido = "{ \"cpf\": ";

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("formato JSON inválido")));
    }

    @Test
    @DisplayName("POST /representantes com ID pré-preenchido no corpo deve sobrescrever o ID com valor gerado pelo banco")
    void cadastrar_comIdInformadoNoCorpo_ignoraIdEAtribuiNovoPeloBanco() throws Exception {
        // Arrange
        Representante rep = new Representante(999L, "99911122233", "Representante ID Novo");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(999)))
                .andExpect(jsonPath("$.cpf").value("99911122233"));
    }

    // -------------------------------------------------------------------------
    // Métricas — Micrometer Counter (representantes.cadastrados.total)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /representantes com sucesso deve incrementar o contador representantes.cadastrados.total por delta (+1.0)")
    void cadastrar_representanteValido_incrementaContadorMetricaEmUm() throws Exception {
        // Arrange
        // Lê o valor atual do contador sem assumir valor zero absoluto, pois
        // o MeterRegistry é compartilhado entre testes e outros testes já podem ter executado
        double countAntes = meterRegistry.get("representantes.cadastrados.total").counter().count();
        Representante rep = new Representante("99988877755", "Fernanda Souza");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rep)));

        // Assert
        resultado.andExpect(status().isCreated());
        double countDepois = meterRegistry.get("representantes.cadastrados.total").counter().count();
        assertThat("O contador deve incrementar exatamente 1 unidade em relação ao valor anterior",
                countDepois, is(countAntes + 1.0));
    }

    @Test
    @DisplayName("POST /representantes com dados inválidos não deve incrementar o contador de métricas")
    void cadastrar_representanteInvalido_naoIncrementaContadorMetrica() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("representantes.cadastrados.total").counter().count();
        Representante repInvalido = new Representante("", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/representantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repInvalido)));

        // Assert
        resultado.andExpect(status().isBadRequest());
        double countDepois = meterRegistry.get("representantes.cadastrados.total").counter().count();
        assertThat("O contador de métricas não deve sofrer alteração quando a validação falhar",
                countDepois, is(countAntes));
    }

    @Test
    @DisplayName("POST /representantes múltiplos cadastros válidos devem incrementar contador por delta relativo sem depender de valor absoluto")
    void cadastrar_multiplosCadastrosValidos_incrementaContadorPorDeltaRelativo() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("representantes.cadastrados.total").counter().count();
        Representante repA = new Representante("77711122233", "Representante Delta A");
        Representante repB = new Representante("77711122244", "Representante Delta B");

        // Act
        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repA)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repB)))
                .andExpect(status().isCreated());

        // Assert
        double countDepois = meterRegistry.get("representantes.cadastrados.total").counter().count();
        assertThat("O contador deve incrementar exatamente 2 unidades acima do valor pré-existente",
                countDepois, is(countAntes + 2.0));
    }

    // -------------------------------------------------------------------------
    // GET /representantes — listagem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /representantes deve retornar lista com todos os representantes cadastrados")
    void listarTodos_comDoisRepresentantes_retornaListaComDoisItens() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("11111111111", "Camila Rocha"));
        representanteRepository.save(new Representante("22222222222", "Marcos Silva"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cpf").value("11111111111"))
                .andExpect(jsonPath("$[1].cpf").value("22222222222"));
    }

    // -------------------------------------------------------------------------
    // GET /representantes/{id} — busca por ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /representantes/{id} com ID existente deve retornar o representante correspondente")
    void buscarPorId_idExistente_retornaRepresentanteCorreto() throws Exception {
        // Arrange
        Representante salvo = representanteRepository.save(new Representante("33333333333", "Thiago Pereira"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/{id}", salvo.getId()));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.cpf").value("33333333333"))
                .andExpect(jsonPath("$.nome").value("Thiago Pereira"));
    }

    @Test
    @DisplayName("GET /representantes/{id} com ID inexistente deve retornar 404 Not Found")
    void buscarPorId_idInexistente_retorna404() throws Exception {
        // Arrange — banco de dados limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/9999"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        containsString("Representante não encontrado com o ID: 9999")));
    }

    // -------------------------------------------------------------------------
    // GET /representantes/nome/{nome} — busca parcial case-insensitive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /representantes/nome/{nome} com termo em minúsculas deve encontrar representantes (case-insensitive)")
    void buscarPorNome_termoEmMinusculas_retornaRepresentantesQueContemOTermo() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("44444444444", "Fernanda Santos"));
        representanteRepository.save(new Representante("55555555555", "Luiz FERNANDO"));
        representanteRepository.save(new Representante("66666666666", "Gabriel Toledo"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/nome/fernand"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));
    }

    @Test
    @DisplayName("GET /representantes/nome/{nome} com termo em maiúsculas deve encontrar representantes (case-insensitive)")
    void buscarPorNome_termoEmMaiusculas_retornaRepresentantesQueContemOTermo() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("44444444444", "Fernanda Santos"));
        representanteRepository.save(new Representante("55555555555", "Luiz FERNANDO"));
        representanteRepository.save(new Representante("66666666666", "Gabriel Toledo"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/nome/TOLEDO"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("66666666666"));
    }

    @Test
    @DisplayName("GET /representantes/nome/{nome} com termo em mixed-case alternado deve encontrar representantes ignorando sensibilidade de caixa")
    void buscarPorNome_termoEmMixedCase_retornaRepresentantesQueContemOTermo() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("44444444444", "Fernanda Santos"));
        representanteRepository.save(new Representante("55555555555", "Luiz FERNANDO"));
        representanteRepository.save(new Representante("66666666666", "Gabriel Toledo"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/nome/fErNaNd"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));
    }

    @Test
    @DisplayName("GET /representantes/nome/{nome} com substring no meio em mixed-case deve encontrar representante correspondente")
    void buscarPorNome_substringMeioEmMixedCase_retornaRepresentanteCorrespondente() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("44444444444", "Fernanda Santos"));
        representanteRepository.save(new Representante("66666666666", "Gabriel Toledo"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/nome/tOlEdO"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("66666666666"));
    }

    @Test
    @DisplayName("GET /representantes/nome/{nome} com termo inexistente deve retornar lista vazia")
    void buscarPorNome_termoInexistente_retornaListaVazia() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("44444444444", "Carlos Andrade"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/nome/INEXISTENTE"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // GET /representantes/cpf/{cpf} — busca exata por CPF
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /representantes/cpf/{cpf} com CPF exato deve retornar o representante correspondente")
    void buscarPorCpf_cpfExistente_retornaRepresentanteCorreto() throws Exception {
        // Arrange
        representanteRepository.save(new Representante("77788899900", "Eduardo Paes"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/cpf/77788899900"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("77788899900"))
                .andExpect(jsonPath("$.nome").value("Eduardo Paes"));
    }

    @Test
    @DisplayName("GET /representantes/cpf/{cpf} com CPF inexistente deve retornar 404 Not Found")
    void buscarPorCpf_cpfInexistente_retorna404() throws Exception {
        // Arrange — banco de dados limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/representantes/cpf/00000000000"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        containsString("Representante não encontrado com o CPF: 00000000000")));
    }
}
