package com.sistemapecas.representantes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemapecas.representantes.model.Representante;
import com.sistemapecas.representantes.repository.RepresentanteRepository;
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
 * Testes de integração para os endpoints REST do representantes-service.
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

    @BeforeEach
    void setUp() {
        representanteRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /representantes deve cadastrar um novo representante e retornar 201 Created")
    void cadastrarRepresentanteComSucesso() throws Exception {
        Representante rep = new Representante("12345678901", "Roberto Dias");

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rep)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.nome").value("Roberto Dias"));
    }

    @Test
    @DisplayName("POST /representantes sem cpf deve retornar 400 com mensagem de erro clara")
    void cadastrarRepresentanteSemCpfDeveFalhar() throws Exception {
        Representante rep = new Representante(null, "Juliana Mendes");

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rep)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CPF é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes sem nome deve retornar 400 com mensagem de erro clara")
    void cadastrarRepresentanteSemNomeDeveFalhar() throws Exception {
        Representante rep = new Representante("98765432100", null);

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rep)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nome é obrigatório")));
    }

    @Test
    @DisplayName("POST /representantes com CPF duplicado deve ser rejeitado com 400")
    void cadastrarRepresentanteComCpfDuplicadoDeveSerRejeitado() throws Exception {
        Representante rep1 = new Representante("11122233344", "Lucas Almeida");
        representanteRepository.save(rep1);

        Representante rep2 = new Representante("11122233344", "Lucas Outro");

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rep2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Já existe um representante cadastrado com o CPF")));
    }

    @Test
    @DisplayName("GET /representantes deve retornar lista de todos os representantes")
    void listarTodosRepresentantes() throws Exception {
        representanteRepository.save(new Representante("11111111111", "Camila Rocha"));
        representanteRepository.save(new Representante("22222222222", "Marcos Silva"));

        mockMvc.perform(get("/representantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cpf").value("11111111111"))
                .andExpect(jsonPath("$[1].cpf").value("22222222222"));
    }

    @Test
    @DisplayName("GET /representantes/{id} deve retornar o representante quando encontrado")
    void buscarPorIdExistente() throws Exception {
        Representante salvo = representanteRepository.save(new Representante("33333333333", "Thiago Pereira"));

        mockMvc.perform(get("/representantes/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.cpf").value("33333333333"))
                .andExpect(jsonPath("$.nome").value("Thiago Pereira"));
    }

    @Test
    @DisplayName("GET /representantes/{id} deve retornar 404 quando não encontrado")
    void buscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/representantes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Representante não encontrado com o ID: 9999")));
    }

    @Test
    @DisplayName("GET /representantes/nome/{nome} deve realizar busca parcial case-insensitive")
    void buscarPorNomeCaseInsensitivePartialMatch() throws Exception {
        representanteRepository.save(new Representante("44444444444", "Fernanda Santos"));
        representanteRepository.save(new Representante("55555555555", "Luiz FERNANDO"));
        representanteRepository.save(new Representante("66666666666", "Gabriel Toledo"));

        // Busca com termo em minúsculo "fernand"
        mockMvc.perform(get("/representantes/nome/fernand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].cpf", containsInAnyOrder("44444444444", "55555555555")));

        // Busca com parte do termo "TOLEDO"
        mockMvc.perform(get("/representantes/nome/TOLEDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value("66666666666"));
    }

    @Test
    @DisplayName("GET /representantes/cpf/{cpf} deve retornar o representante se CPF exato")
    void buscarPorCpfExistente() throws Exception {
        representanteRepository.save(new Representante("77788899900", "Eduardo Paes"));

        mockMvc.perform(get("/representantes/cpf/77788899900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("77788899900"))
                .andExpect(jsonPath("$.nome").value("Eduardo Paes"));
    }

    @Test
    @DisplayName("GET /representantes/cpf/{cpf} deve retornar 404 se não encontrado")
    void buscarPorCpfInexistente() throws Exception {
        mockMvc.perform(get("/representantes/cpf/00000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Representante não encontrado com o CPF: 00000000000")));
    }
}
