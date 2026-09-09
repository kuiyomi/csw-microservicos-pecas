package com.sistemapecas.pecas.controller;

import com.sistemapecas.pecas.model.Peca;
import com.sistemapecas.pecas.service.PecaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST que expõe os endpoints para gerenciamento de Peças.
 * Mapeamento base: /pecas
 */
@RestController
@RequestMapping("/pecas")
public class PecaController {

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    /**
     * Endpoint para cadastrar uma nova peça.
     * Retorna status HTTP 201 (Created) e o objeto da peça persistida.
     *
     * @param peca Objeto da peça com validação de campos obrigatórios (@Valid)
     * @return ResponseEntity contendo a peça criada e status 201
     */
    @PostMapping
    public ResponseEntity<Peca> cadastrar(@Valid @RequestBody Peca peca) {
        Peca novaPeca = pecaService.cadastrar(peca);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaPeca);
    }

    /**
     * Endpoint para listar todas as peças cadastradas.
     *
     * @return Lista com todas as peças cadastradas e status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Peca>> listarTodas() {
        List<Peca> pecas = pecaService.listarTodas();
        return ResponseEntity.ok(pecas);
    }

    /**
     * Endpoint para buscar uma peça pelo seu ID numérico.
     * Retorna status HTTP 404 (Not Found) caso a peça não exista.
     *
     * @param id Identificador numérico da peça
     * @return A peça encontrada e status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<Peca> buscarPorId(@PathVariable Long id) {
        Peca peca = pecaService.buscarPorId(id);
        return ResponseEntity.ok(peca);
    }

    /**
     * Endpoint para buscar peças pelo nome com correspondência parcial e case-insensitive.
     *
     * @param nome Nome ou parte do nome da peça
     * @return Lista de peças correspondentes e status 200 OK
     */
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Peca>> buscarPorNome(@PathVariable String nome) {
        List<Peca> pecas = pecaService.buscarPorNome(nome);
        return ResponseEntity.ok(pecas);
    }

    /**
     * Endpoint para buscar uma peça pelo seu número de identificação único exato.
     * Retorna status HTTP 404 (Not Found) caso a peça não exista.
     *
     * @param nroIdentificacao Número de identificação exato da peça
     * @return A peça encontrada e status 200 OK
     */
    @GetMapping("/identificacao/{nroIdentificacao}")
    public ResponseEntity<Peca> buscarPorIdentificacao(@PathVariable String nroIdentificacao) {
        Peca peca = pecaService.buscarPorNroIdentificacao(nroIdentificacao);
        return ResponseEntity.ok(peca);
    }
}
