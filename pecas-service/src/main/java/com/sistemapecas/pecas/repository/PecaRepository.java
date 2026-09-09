package com.sistemapecas.pecas.repository;

import com.sistemapecas.pecas.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade Peca.
 * Fornece métodos CRUD padrão e consultas derivadas por convenção de nomenclatura.
 */
@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {

    /**
     * Busca uma peça pelo seu número de identificação exato.
     *
     * @param nroIdentificacao Número de identificação da peça
     * @return Optional contendo a peça se encontrada, ou vazio caso contrário
     */
    Optional<Peca> findByNroIdentificacao(String nroIdentificacao);

    /**
     * Verifica se já existe uma peça cadastrada com o número de identificação fornecido.
     *
     * @param nroIdentificacao Número de identificação a ser verificado
     * @return true se já existir uma peça com o código informado, false caso contrário
     */
    boolean existsByNroIdentificacao(String nroIdentificacao);

    /**
     * Busca peças cujo nome contenha o termo pesquisado, ignorando maiúsculas e minúsculas
     * (case-insensitive partial match).
     *
     * @param nome Termo ou parte do nome da peça
     * @return Lista de peças que contêm o termo pesquisado em seu nome
     */
    List<Peca> findByNomeContainingIgnoreCase(String nome);
}
