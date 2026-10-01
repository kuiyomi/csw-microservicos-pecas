package com.sistemapecas.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.clientes.model.Cliente;
import com.sistemapecas.clientes.repository.ClienteRepository;
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
 * Testes de integração para os endpoints REST do clientes-service.
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
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // POST /clientes — cadastro e validações
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /clientes com dados válidos deve retornar 201 Created com o corpo do cliente")
    void cadastrar_clienteValido_retorna201ComCorpo() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("12345678901", "Carlos Silva");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Carlos Silva"));
    }

    @Test
    @DisplayName("POST /clientes com CPF nulo deve retornar 400 Bad Request")
    void cadastrar_cpfNulo_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente(null, "Mariana Costa");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com CPF vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_cpfVazio_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("", "Mariana Costa");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com CPF apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_cpfSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("   ", "Mariana Costa");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com CPF contendo tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_cpfComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente(" \t \n ", "Mariana Costa");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com nome nulo deve retornar 400 Bad Request")
    void cadastrar_nomeNulo_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("98765432100", null);

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com nome vazio (string vazia \"\") deve retornar 400 Bad Request")
    void cadastrar_nomeVazio_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("98765432100", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com nome apenas espaços em branco deve retornar 400 Bad Request")
    void cadastrar_nomeSomenteEspacos_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("98765432100", "   ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com nome contendo tabulações e quebras de linha deve retornar 400 Bad Request")
    void cadastrar_nomeComTabulacoesEEspacos_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("98765432100", " \t \n ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com ambos os campos obrigatórios nulos deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosNulos_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente(null, null);

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /clientes com ambos os campos obrigatórios vazios deve retornar 400 Bad Request")
    void cadastrar_camposObrigatoriosAmbosVazios_retorna400() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /clientes com espaços excedentes nas extremidades deve aparar os campos e salvar com sucesso")
    void cadastrar_camposComEspacosNasExtremidades_salvaComCamposAparados() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("  12345678901  ", "  Carlos Alberto Silva  ");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Carlos Alberto Silva"));
    }

    @Test
    @DisplayName("POST /clientes com CPF duplicado deve retornar 400 Bad Request indicando conflito")
    void cadastrar_cpfDuplicado_retorna400ComMensagemConflito() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("11122233344", "João da Silva"));
        Cliente cliente2 = new Cliente("11122233344", "João Souza");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente2)));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Já existe um cliente cadastrado com o CPF")));
    }

    @Test
    @DisplayName("POST /clientes com corpo contendo JSON malformado deve retornar 400 Bad Request")
    void cadastrar_jsonMalformado_retorna400() throws Exception {
        // Arrange
        String jsonInvalido = "{ \"cpf\": ";

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido));

        // Assert
        resultado.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("formato JSON inválido")));
    }

    @Test
    @DisplayName("POST /clientes com ID pré-preenchido no corpo deve sobrescrever o ID com valor gerado pelo banco")
    void cadastrar_comIdInformadoNoCorpo_ignoraIdEAtribuiNovoPeloBanco() throws Exception {
        // Arrange
        Cliente cliente = new Cliente(999L, "99911122233", "Cliente ID Novo");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(999)))
                .andExpect(jsonPath("$.cpf").value("99911122233"));
    }

    // -------------------------------------------------------------------------
    // Métricas — Micrometer Counter (clientes.cadastrados.total)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /clientes com sucesso deve incrementar o contador clientes.cadastrados.total por delta (+1.0)")
    void cadastrar_clienteValido_incrementaContadorMetricaEmUm() throws Exception {
        // Arrange
        // Lê o valor atual do contador sem assumir valor zero absoluto, pois
        // o MeterRegistry é compartilhado entre testes e outros testes já podem ter executado
        double countAntes = meterRegistry.get("clientes.cadastrados.total").counter().count();
        Cliente cliente = new Cliente("99988877766", "Mariana Lima");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        // Assert
        resultado.andExpect(status().isCreated());
        double countDepois = meterRegistry.get("clientes.cadastrados.total").counter().count();
        assertThat("O contador deve incrementar exatamente 1 unidade em relação ao valor anterior",
                countDepois, is(countAntes + 1.0));
    }

    @Test
    @DisplayName("POST /clientes com dados inválidos não deve incrementar o contador de métricas")
    void cadastrar_clienteInvalido_naoIncrementaContadorMetrica() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("clientes.cadastrados.total").counter().count();
        Cliente clienteInvalido = new Cliente("", "");

        // Act
        ResultActions resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteInvalido)));

        // Assert
        resultado.andExpect(status().isBadRequest());
        double countDepois = meterRegistry.get("clientes.cadastrados.total").counter().count();
        assertThat("O contador de métricas não deve sofrer alteração quando a validação falhar",
                countDepois, is(countAntes));
    }

    @Test
    @DisplayName("POST /clientes múltiplos cadastros válidos devem incrementar contador por delta relativo sem depender de valor absoluto")
    void cadastrar_multiplosCadastrosValidos_incrementaContadorPorDeltaRelativo() throws Exception {
        // Arrange
        double countAntes = meterRegistry.get("clientes.cadastrados.total").counter().count();
        Cliente clienteA = new Cliente("88811122233", "Cliente Delta A");
        Cliente clienteB = new Cliente("88811122244", "Cliente Delta B");

        // Act
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteA)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteB)))
                .andExpect(status().isCreated());

        // Assert
        double countDepois = meterRegistry.get("clientes.cadastrados.total").counter().count();
        assertThat("O contador deve incrementar exatamente 2 unidades acima do valor pré-existente",
                countDepois, is(countAntes + 2.0));
    }

    // -------------------------------------------------------------------------
    // GET /clientes — listagem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /clientes deve retornar lista com todos os clientes cadastrados")
    void listarTodos_comDoisClientes_retornaListaComDoisItens() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("11111111111", "Ana Santos"));
        clienteRepository.save(new Cliente("22222222222", "Bruno Lima"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cpf").value("11111111111"))
                .andExpect(jsonPath("$[1].cpf").value("22222222222"));
    }

    // -------------------------------------------------------------------------
    // GET /clientes/{id} — busca por ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /clientes/{id} com ID existente deve retornar o cliente correspondente")
    void buscarPorId_idExistente_retornaClienteCorreto() throws Exception {
        // Arrange
        Cliente salvo = clienteRepository.save(new Cliente("33333333333", "Claudia Ramos"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/{id}", salvo.getId()));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.cpf").value("33333333333"))
                .andExpect(jsonPath("$.nome").value("Claudia Ramos"));
    }

    @Test
    @DisplayName("GET /clientes/{id} com ID inexistente deve retornar 404 Not Found")
    void buscarPorId_idInexistente_retorna404() throws Exception {
        // Arrange — banco de dados limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/9999"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado com o ID: 9999")));
    }

    // -------------------------------------------------------------------------
    // GET /clientes/nome/{nome} — busca parcial case-insensitive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /clientes/nome/{nome} com termo em minúsculas deve encontrar clientes (case-insensitive)")
    void buscarPorNome_termoEmMinusculas_retornaClientesQueContemOTermo() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("44444444444", "Fernanda Oliveira"));
        clienteRepository.save(new Cliente("55555555555", "Lucas FERNANDO"));
        clienteRepository.save(new Cliente("66666666666", "Roberto Carlos"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/nome/fernand"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} com termo em maiúsculas deve encontrar clientes (case-insensitive)")
    void buscarPorNome_termoEmMaiusculas_retornaClientesQueContemOTermo() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("44444444444", "Fernanda Oliveira"));
        clienteRepository.save(new Cliente("55555555555", "Lucas FERNANDO"));
        clienteRepository.save(new Cliente("66666666666", "Roberto Carlos"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/nome/CARLOS"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("66666666666"));
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} com termo em mixed-case alternado deve encontrar clientes ignorando sensibilidade de caixa")
    void buscarPorNome_termoEmMixedCase_retornaClientesQueContemOTermo() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("44444444444", "Fernanda Oliveira"));
        clienteRepository.save(new Cliente("55555555555", "Lucas FERNANDO"));
        clienteRepository.save(new Cliente("66666666666", "Roberto Carlos"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/nome/fErNaNd"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} com substring no meio em mixed-case deve encontrar cliente correspondente")
    void buscarPorNome_substringMeioEmMixedCase_retornaClienteCorrespondente() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("44444444444", "Fernanda Oliveira"));
        clienteRepository.save(new Cliente("55555555555", "Roberto Carlos"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/nome/oLiVeIrA"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("44444444444"));
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} com termo inexistente deve retornar lista vazia")
    void buscarPorNome_termoInexistente_retornaListaVazia() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("44444444444", "Ana Pereira"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/nome/MARIANA"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // GET /clientes/cpf/{cpf} — busca exata por CPF
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} com CPF exato deve retornar o cliente correspondente")
    void buscarPorCpf_cpfExistente_retornaClienteCorreto() throws Exception {
        // Arrange
        clienteRepository.save(new Cliente("77788899900", "Diego Martins"));

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/cpf/77788899900"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("77788899900"))
                .andExpect(jsonPath("$.nome").value("Diego Martins"));
    }

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} com CPF inexistente deve retornar 404 Not Found")
    void buscarPorCpf_cpfInexistente_retorna404() throws Exception {
        // Arrange — banco de dados limpo pelo @BeforeEach

        // Act
        ResultActions resultado = mockMvc.perform(get("/clientes/cpf/00000000000"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        containsString("Cliente não encontrado com o CPF: 00000000000")));
    }
}
