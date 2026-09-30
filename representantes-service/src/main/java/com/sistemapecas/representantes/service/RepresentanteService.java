package com.sistemapecas.representantes.service;

import com.sistemapecas.representantes.exception.DuplicateCpfException;
import com.sistemapecas.representantes.exception.ResourceNotFoundException;
import com.sistemapecas.representantes.model.Representante;
import com.sistemapecas.representantes.repository.RepresentanteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço responsável pelas regras de negócio relacionadas a Representantes comerciais.
 */
@Service
public class RepresentanteService {

    private final RepresentanteRepository representanteRepository;
    private final Counter representantesCadastradosCounter;

    public RepresentanteService(RepresentanteRepository representanteRepository, MeterRegistry meterRegistry) {
        this.representanteRepository = representanteRepository;
        this.representantesCadastradosCounter = Counter.builder("representantes.cadastrados.total")
                .description("Total de representantes cadastrados com sucesso")
                .register(meterRegistry);
    }

    /**
     * Cadastra um novo representante comercial no banco de dados.
     * Valida se os campos obrigatórios estão preenchidos e se o CPF é único.
     *
     * @param representante Dados do representante a ser cadastrado
     * @return O representante persistido com seu ID gerado
     * @throws DuplicateCpfException     caso o CPF já esteja em uso
     * @throws IllegalArgumentException caso algum campo obrigatório não seja preenchido
     */
    @Transactional
    public Representante cadastrar(Representante representante) {
        if (representante == null) {
            throw new IllegalArgumentException("Dados do representante não podem ser nulos");
        }

        // Validação defensiva dos campos obrigatórios
        if (representante.getCpf() == null || representante.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF é obrigatório");
        }
        if (representante.getNome() == null || representante.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        // Verifica duplicidade do CPF
        if (representanteRepository.existsByCpf(representante.getCpf().trim())) {
            throw new DuplicateCpfException(
                    "Já existe um representante cadastrado com o CPF: " + representante.getCpf().trim()
            );
        }

        // Garante que o ID seja gerado pelo banco de dados
        representante.setId(null);
        representante.setCpf(representante.getCpf().trim());
        representante.setNome(representante.getNome().trim());

        Representante representanteSalvo = representanteRepository.save(representante);
        representantesCadastradosCounter.increment();
        return representanteSalvo;
    }

    /**
     * Lista todos os representantes comerciais cadastrados no sistema.
     *
     * @return Lista com todos os representantes
     */
    @Transactional(readOnly = true)
    public List<Representante> listarTodos() {
        return representanteRepository.findAll();
    }

    /**
     * Busca um representante pelo seu identificador único (ID).
     *
     * @param id Identificador numérico do representante
     * @return O representante encontrado
     * @throws ResourceNotFoundException caso o representante não seja encontrado
     */
    @Transactional(readOnly = true)
    public Representante buscarPorId(Long id) {
        return representanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Representante não encontrado com o ID: " + id));
    }

    /**
     * Busca representantes cujo nome contenha o termo especificado, ignorando maiúsculas e minúsculas.
     *
     * @param nome Termo ou parte do nome do representante
     * @return Lista de representantes correspondentes à busca
     */
    @Transactional(readOnly = true)
    public List<Representante> buscarPorNome(String nome) {
        return representanteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Busca um representante pelo seu CPF único exato.
     *
     * @param cpf CPF do representante
     * @return O representante encontrado
     * @throws ResourceNotFoundException caso o representante não seja encontrado
     */
    @Transactional(readOnly = true)
    public Representante buscarPorCpf(String cpf) {
        return representanteRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Representante não encontrado com o CPF: " + cpf
                ));
    }
}
