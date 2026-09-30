package com.sistemapecas.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.clientes.model.Cliente;
import com.sistemapecas.clientes.repository.ClienteRepository;
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
 * Testes de integração para os endpoints REST do clientes-service.
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
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /clientes deve incrementar o contador clientes.cadastrados.total ao cadastrar cliente")
    void cadastrar_clienteValido_deveIncrementarContadorMetrica() throws Exception {
        // Arrange
        double countInicial = meterRegistry.get("clientes.cadastrados.total").counter().count();
        Cliente cliente = new Cliente("99988877766", "Mariana Lima");

        // Act
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated());

        // Assert
        double countFinal = meterRegistry.get("clientes.cadastrados.total").counter().count();
        org.hamcrest.MatcherAssert.assertThat(countFinal, is(countInicial + 1.0));
    }

    @Test
    @DisplayName("POST /clientes deve cadastrar um novo cliente e retornar 201 Created")
    void cadastrarClienteComSucesso() throws Exception {
        Cliente cliente = new Cliente("12345678901", "Carlos Silva");

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Carlos Silva"));
    }

    @Test
    @DisplayName("POST /clientes sem cpf deve retornar 400 com mensagem de erro clara")
    void cadastrarClienteSemCpfDeveFalhar() throws Exception {
        Cliente cliente = new Cliente(null, "Mariana Costa");

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes sem nome deve retornar 400 com mensagem de erro clara")
    void cadastrarClienteSemNomeDeveFalhar() throws Exception {
        Cliente cliente = new Cliente("98765432100", null);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /clientes com CPF duplicado deve ser rejeitado com 400")
    void cadastrarClienteComCpfDuplicadoDeveSerRejeitado() throws Exception {
        Cliente cliente1 = new Cliente("11122233344", "João da Silva");
        clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente("11122233344", "João Souza");

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Já existe um cliente cadastrado com o CPF")));
    }

    @Test
    @DisplayName("GET /clientes deve retornar lista de todos os clientes")
    void listarTodosClientes() throws Exception {
        clienteRepository.save(new Cliente("11111111111", "Ana Santos"));
        clienteRepository.save(new Cliente("22222222222", "Bruno Lima"));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cpf").value("11111111111"))
                .andExpect(jsonPath("$[1].cpf").value("22222222222"));
    }

    @Test
    @DisplayName("GET /clientes/{id} deve retornar o cliente quando encontrado")
    void buscarPorIdExistente() throws Exception {
        Cliente salvo = clienteRepository.save(new Cliente("33333333333", "Claudia Ramos"));

        mockMvc.perform(get("/clientes/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.cpf").value("33333333333"))
                .andExpect(jsonPath("$.nome").value("Claudia Ramos"));
    }

    @Test
    @DisplayName("GET /clientes/{id} deve retornar 404 quando não encontrado")
    void buscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/clientes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado com o ID: 9999")));
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} deve realizar busca parcial case-insensitive")
    void buscarPorNomeCaseInsensitivePartialMatch() throws Exception {
        clienteRepository.save(new Cliente("44444444444", "Fernanda Oliveira"));
        clienteRepository.save(new Cliente("55555555555", "Lucas FERNANDO"));
        clienteRepository.save(new Cliente("66666666666", "Roberto Carlos"));

        // Busca com termo em minúsculo "fernand"
        mockMvc.perform(get("/clientes/nome/fernand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));

        // Busca com parte do termo "CARLOS"
        mockMvc.perform(get("/clientes/nome/CARLOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("66666666666"));
    }

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} deve retornar o cliente se CPF exato")
    void buscarPorCpfExistente() throws Exception {
        clienteRepository.save(new Cliente("77788899900", "Diego Martins"));

        mockMvc.perform(get("/clientes/cpf/77788899900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("77788899900"))
                .andExpect(jsonPath("$.nome").value("Diego Martins"));
    }

    @Test
    @DisplayName("GET /clientes/cpf/{cpf} deve retornar 404 se não encontrado")
    void buscarPorCpfInexistente() throws Exception {
        mockMvc.perform(get("/clientes/cpf/00000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado com o CPF: 00000000000")));
    }
}
