package com.sistemapecas.pecas.service;

import com.sistemapecas.pecas.exception.DuplicateIdentificationException;
import com.sistemapecas.pecas.exception.ResourceNotFoundException;
import com.sistemapecas.pecas.model.Peca;
import com.sistemapecas.pecas.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço responsável pelas regras de negócio relacionadas a Peças.
 */
@Service
public class PecaService {

    private final PecaRepository pecaRepository;

    public PecaService(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    /**
     * Cadastra uma nova peça no banco de dados.
     * Valida se os campos obrigatórios estão preenchidos e se o número de identificação é único.
     *
     * @param peca Dados da peça a ser cadastrada
     * @return A peça persistida com seu ID gerado
     * @throws DuplicateIdentificationException caso o número de identificação já esteja em uso
     * @throws IllegalArgumentException         caso algum campo obrigatório não seja preenchido
     */
    @Transactional
    public Peca cadastrar(Peca peca) {
        if (peca == null) {
            throw new IllegalArgumentException("Dados da peça não podem ser nulos");
        }

        // Validação defensiva dos campos obrigatórios
        if (peca.getNroIdentificacao() == null || peca.getNroIdentificacao().trim().isEmpty()) {
            throw new IllegalArgumentException("O número de identificação é obrigatório");
        }
        if (peca.getNome() == null || peca.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        // Verifica duplicidade do número de identificação
        if (pecaRepository.existsByNroIdentificacao(peca.getNroIdentificacao().trim())) {
            throw new DuplicateIdentificationException(
                    "Já existe uma peça cadastrada com o número de identificação: " + peca.getNroIdentificacao().trim()
            );
        }

        // Garante que o ID seja gerado pelo banco de dados
        peca.setId(null);
        peca.setNroIdentificacao(peca.getNroIdentificacao().trim());
        peca.setNome(peca.getNome().trim());

        return pecaRepository.save(peca);
    }

    /**
     * Lista todas as peças cadastradas no sistema.
     *
     * @return Lista com todas as peças
     */
    @Transactional(readOnly = true)
    public List<Peca> listarTodas() {
        return pecaRepository.findAll();
    }

    /**
     * Busca uma peça pelo seu identificador único (ID).
     *
     * @param id Identificador numérico da peça
     * @return A peça encontrada
     * @throws ResourceNotFoundException caso a peça não seja encontrada
     */
    @Transactional(readOnly = true)
    public Peca buscarPorId(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada com o ID: " + id));
    }

    /**
     * Busca peças cujo nome contenha o termo especificado, ignorando maiúsculas e minúsculas.
     *
     * @param nome Termo ou parte do nome da peça
     * @return Lista de peças correspondentes à busca
     */
    @Transactional(readOnly = true)
    public List<Peca> buscarPorNome(String nome) {
        return pecaRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Busca uma peça pelo seu número de identificação único exato.
     *
     * @param nroIdentificacao Número de identificação da peça
     * @return A peça encontrada
     * @throws ResourceNotFoundException caso a peça não seja encontrada
     */
    @Transactional(readOnly = true)
    public Peca buscarPorNroIdentificacao(String nroIdentificacao) {
        return pecaRepository.findByNroIdentificacao(nroIdentificacao)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Peça não encontrada com o número de identificação: " + nroIdentificacao
                ));
    }
}
