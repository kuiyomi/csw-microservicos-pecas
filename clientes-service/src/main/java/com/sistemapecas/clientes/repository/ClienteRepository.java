package com.sistemapecas.clientes.repository;

import com.sistemapecas.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade Cliente.
 * Fornece operações CRUD padrão e consultas derivadas por convenção de nomenclatura.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca um cliente pelo seu CPF exato.
     *
     * @param cpf CPF do cliente
     * @return Optional contendo o cliente se encontrado, ou vazio caso contrário
     */
    Optional<Cliente> findByCpf(String cpf);

    /**
     * Verifica se já existe um cliente cadastrado com o CPF fornecido.
     *
     * @param cpf CPF a ser verificado
     * @return true se já existir um cliente com o CPF informado, false caso contrário
     */
    boolean existsByCpf(String cpf);

    /**
     * Busca clientes cujo nome contenha o termo pesquisado, ignorando maiúsculas e minúsculas
     * (case-insensitive partial match).
     *
     * @param nome Termo ou parte do nome do cliente
     * @return Lista de clientes que contêm o termo pesquisado em seu nome
     */
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
