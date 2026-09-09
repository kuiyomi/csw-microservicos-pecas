package com.sistemapecas.representantes.repository;

import com.sistemapecas.representantes.model.Representante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade Representante.
 * Fornece operações CRUD padrão e consultas derivadas por convenção de nomenclatura.
 */
@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, Long> {

    /**
     * Busca um representante pelo seu CPF exato.
     *
     * @param cpf CPF do representante
     * @return Optional contendo o representante se encontrado, ou vazio caso contrário
     */
    Optional<Representante> findByCpf(String cpf);

    /**
     * Verifica se já existe um representante cadastrado com o CPF fornecido.
     *
     * @param cpf CPF a ser verificado
     * @return true se já existir um representante com o CPF informado, false caso contrário
     */
    boolean existsByCpf(String cpf);

    /**
     * Busca representantes cujo nome contenha o termo pesquisado, ignorando maiúsculas e minúsculas
     * (case-insensitive partial match).
     *
     * @param nome Termo ou parte do nome do representante
     * @return Lista de representantes que contêm o termo pesquisado em seu nome
     */
    List<Representante> findByNomeContainingIgnoreCase(String nome);
}
