package com.sistemapecas.clientes.controller;

import com.sistemapecas.clientes.model.Cliente;
import com.sistemapecas.clientes.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST que expõe os endpoints para gerenciamento de Clientes.
 * Mapeamento base: /clientes
 */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Endpoint para cadastrar um novo cliente.
     * Retorna status HTTP 201 (Created) e o objeto do cliente persistido.
     * Rejeita CPF duplicado com status HTTP 400 (Bad Request).
     *
     * @param cliente Objeto do cliente com validação de campos obrigatórios (@Valid)
     * @return ResponseEntity contendo o cliente criado e status 201
     */
    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@Valid @RequestBody Cliente cliente) {
        Cliente novoCliente = clienteService.cadastrar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
    }

    /**
     * Endpoint para listar todos os clientes cadastrados.
     *
     * @return Lista com todos os clientes cadastrados e status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Endpoint para buscar um cliente pelo seu ID numérico.
     * Retorna status HTTP 404 (Not Found) caso o cliente não exista.
     *
     * @param id Identificador numérico do cliente
     * @return O cliente encontrado e status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Endpoint para buscar clientes pelo nome com correspondência parcial e case-insensitive.
     *
     * @param nome Nome ou parte do nome do cliente
     * @return Lista de clientes correspondentes e status 200 OK
     */
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Cliente>> buscarPorNome(@PathVariable String nome) {
        List<Cliente> clientes = clienteService.buscarPorNome(nome);
        return ResponseEntity.ok(clientes);
    }

    /**
     * Endpoint para buscar um cliente pelo seu CPF exato.
     * Retorna status HTTP 404 (Not Found) caso o cliente não exista.
     *
     * @param cpf CPF exato do cliente
     * @return O cliente encontrado e status 200 OK
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> buscarPorCpf(@PathVariable String cpf) {
        Cliente cliente = clienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(cliente);
    }
}
