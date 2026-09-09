package com.sistemapecas.clientes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Entidade que representa um Cliente no sistema.
 * Mapeada para a tabela 'clientes' no banco de dados H2.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    /**
     * Identificador único auto-gerado pelo banco de dados (chave primária).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cadastro de Pessoa Física (CPF) do cliente.
     * Campo obrigatório e único no sistema.
     */
    @NotBlank(message = "O CPF é obrigatório")
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    /**
     * Nome completo do cliente.
     * Campo obrigatório.
     */
    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String nome;

    /**
     * Construtor padrão sem argumentos (exigido pela especificação JPA).
     */
    public Cliente() {
    }

    /**
     * Construtor de conveniência sem o campo ID (para criação de novos clientes).
     *
     * @param cpf  CPF único do cliente
     * @param nome Nome do cliente
     */
    public Cliente(String cpf, String nome) {
        this.cpf = cpf;
        this.nome = nome;
    }

    /**
     * Construtor completo com todos os atributos.
     *
     * @param id   Identificador único
     * @param cpf  CPF único do cliente
     * @param nome Nome do cliente
     */
    public Cliente(Long id, String cpf, String nome) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
    }

    // --- Métodos Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // --- Métodos equals, hashCode e toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id) && Objects.equals(cpf, cliente.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", cpf='" + cpf + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
