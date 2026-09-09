package com.sistemapecas.representantes.controller;

import com.sistemapecas.representantes.model.Representante;
import com.sistemapecas.representantes.service.RepresentanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST que expõe os endpoints para gerenciamento de Representantes comerciais.
 * Mapeamento base: /representantes
 */
@RestController
@RequestMapping("/representantes")
public class RepresentanteController {

    private final RepresentanteService representanteService;

    public RepresentanteController(RepresentanteService representanteService) {
        this.representanteService = representanteService;
    }

    /**
     * Endpoint para cadastrar um novo representante comercial.
     * Retorna status HTTP 201 (Created) e o objeto do representante persistido.
     * Rejeita CPF duplicado com status HTTP 400 (Bad Request).
     *
     * @param representante Objeto do representante com validação de campos obrigatórios (@Valid)
     * @return ResponseEntity contendo o representante criado e status 201
     */
    @PostMapping
    public ResponseEntity<Representante> cadastrar(@Valid @RequestBody Representante representante) {
        Representante novoRepresentante = representanteService.cadastrar(representante);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoRepresentante);
    }

    /**
     * Endpoint para listar todos os representantes comerciais cadastrados.
     *
     * @return Lista com todos os representantes cadastrados e status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Representante>> listarTodos() {
        List<Representante> representantes = representanteService.listarTodos();
        return ResponseEntity.ok(representantes);
    }

    /**
     * Endpoint para buscar um representante pelo seu ID numérico.
     * Retorna status HTTP 404 (Not Found) caso o representante não exista.
     *
     * @param id Identificador numérico do representante
     * @return O representante encontrado e status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<Representante> buscarPorId(@PathVariable Long id) {
        Representante representante = representanteService.buscarPorId(id);
        return ResponseEntity.ok(representante);
    }

    /**
     * Endpoint para buscar representantes pelo nome com correspondência parcial e case-insensitive.
     *
     * @param nome Nome ou parte do nome do representante
     * @return Lista de representantes correspondentes e status 200 OK
     */
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Representante>> buscarPorNome(@PathVariable String nome) {
        List<Representante> representantes = representanteService.buscarPorNome(nome);
        return ResponseEntity.ok(representantes);
    }

    /**
     * Endpoint para buscar um representante pelo seu CPF exato.
     * Retorna status HTTP 404 (Not Found) caso o representante não exista.
     *
     * @param cpf CPF exato do representante
     * @return O representante encontrado e status 200 OK
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Representante> buscarPorCpf(@PathVariable String cpf) {
        Representante representante = representanteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(representante);
    }
}
