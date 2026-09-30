package com.sistemapecas.clientes.service;

import com.sistemapecas.clientes.exception.DuplicateCpfException;
import com.sistemapecas.clientes.exception.ResourceNotFoundException;
import com.sistemapecas.clientes.model.Cliente;
import com.sistemapecas.clientes.repository.ClienteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço responsável pelas regras de negócio relacionadas a Clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final Counter clientesCadastradosCounter;

    public ClienteService(ClienteRepository clienteRepository, MeterRegistry meterRegistry) {
        this.clienteRepository = clienteRepository;
        this.clientesCadastradosCounter = Counter.builder("clientes.cadastrados.total")
                .description("Total de clientes cadastrados com sucesso")
                .register(meterRegistry);
    }

    /**
     * Cadastra um novo cliente no banco de dados.
     * Valida se os campos obrigatórios estão preenchidos e se o CPF é único.
     *
     * @param cliente Dados do cliente a ser cadastrado
     * @return O cliente persistido com seu ID gerado
     * @throws DuplicateCpfException     caso o CPF já esteja em uso
     * @throws IllegalArgumentException caso algum campo obrigatório não seja preenchido
     */
    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Dados do cliente não podem ser nulos");
        }

        // Validação defensiva dos campos obrigatórios
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF é obrigatório");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        // Verifica duplicidade do CPF
        if (clienteRepository.existsByCpf(cliente.getCpf().trim())) {
            throw new DuplicateCpfException(
                    "Já existe um cliente cadastrado com o CPF: " + cliente.getCpf().trim()
            );
        }

        // Garante que o ID seja gerado pelo banco de dados
        cliente.setId(null);
        cliente.setCpf(cliente.getCpf().trim());
        cliente.setNome(cliente.getNome().trim());

        Cliente clienteSalvo = clienteRepository.save(cliente);
        clientesCadastradosCounter.increment();
        return clienteSalvo;
    }

    /**
     * Lista todos os clientes cadastrados no sistema.
     *
     * @return Lista com todos os clientes
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    /**
     * Busca um cliente pelo seu identificador único (ID).
     *
     * @param id Identificador numérico do cliente
     * @return O cliente encontrado
     * @throws ResourceNotFoundException caso o cliente não seja encontrado
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
    }

    /**
     * Busca clientes cujo nome contenha o termo especificado, ignorando maiúsculas e minúsculas.
     *
     * @param nome Termo ou parte do nome do cliente
     * @return Lista de clientes correspondentes à busca
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Busca um cliente pelo seu CPF único exato.
     *
     * @param cpf CPF do cliente
     * @return O cliente encontrado
     * @throws ResourceNotFoundException caso o cliente não seja encontrado
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente não encontrado com o CPF: " + cpf
                ));
    }
}
