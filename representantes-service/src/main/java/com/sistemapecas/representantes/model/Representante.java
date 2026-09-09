package com.sistemapecas.representantes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Entidade que representa um Representante comercial no sistema.
 * Mapeada para a tabela 'representantes' no banco de dados H2.
 */
@Entity
@Table(name = "representantes")
public class Representante {

    /**
     * Identificador único auto-gerado pelo banco de dados (chave primária).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cadastro de Pessoa Física (CPF) do representante comercial.
     * Campo obrigatório e único no sistema.
     */
    @NotBlank(message = "O CPF é obrigatório")
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    /**
     * Nome completo do representante comercial.
     * Campo obrigatório.
     */
    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String nome;

    /**
     * Construtor padrão sem argumentos (exigido pela especificação JPA).
     */
    public Representante() {
    }

    /**
     * Construtor de conveniência sem o campo ID (para criação de novos representantes).
     *
     * @param cpf  CPF único do representante
     * @param nome Nome do representante
     */
    public Representante(String cpf, String nome) {
        this.cpf = cpf;
        this.nome = nome;
    }

    /**
     * Construtor completo com todos os atributos.
     *
     * @param id   Identificador único
     * @param cpf  CPF único do representante
     * @param nome Nome do representante
     */
    public Representante(Long id, String cpf, String nome) {
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
        Representante that = (Representante) o;
        return Objects.equals(id, that.id) && Objects.equals(cpf, that.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }

    @Override
    public String toString() {
        return "Representante{" +
                "id=" + id +
                ", cpf='" + cpf + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
