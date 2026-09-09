package com.sistemapecas.pecas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Entidade que representa uma Peça no catálogo de peças automotivas.
 * Mapeada para a tabela 'pecas' no banco de dados H2.
 */
@Entity
@Table(name = "pecas")
public class Peca {

    /**
     * Identificador único auto-gerado pelo banco de dados (chave primária).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de identificação único da peça (ex: código do fabricante ou SKU).
     * Campo obrigatório e único no sistema.
     */
    @NotBlank(message = "O número de identificação é obrigatório")
    @Column(name = "nro_identificacao", nullable = false, unique = true)
    private String nroIdentificacao;

    /**
     * Nome comercial da peça.
     * Campo obrigatório.
     */
    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String nome;

    /**
     * Descrição detalhada da peça (opcional).
     */
    @Column(name = "descricao")
    private String descricao;

    /**
     * Construtor padrão sem argumentos (exigido pela especificação JPA).
     */
    public Peca() {
    }

    /**
     * Construtor de conveniência sem o campo ID (para criação de novas peças).
     *
     * @param nroIdentificacao Número de identificação único
     * @param nome             Nome da peça
     * @param descricao        Descrição da peça
     */
    public Peca(String nroIdentificacao, String nome, String descricao) {
        this.nroIdentificacao = nroIdentificacao;
        this.nome = nome;
        this.descricao = descricao;
    }

    /**
     * Construtor completo com todos os atributos.
     *
     * @param id               Identificador único
     * @param nroIdentificacao Número de identificação único
     * @param nome             Nome da peça
     * @param descricao        Descrição da peça
     */
    public Peca(Long id, String nroIdentificacao, String nome, String descricao) {
        this.id = id;
        this.nroIdentificacao = nroIdentificacao;
        this.nome = nome;
        this.descricao = descricao;
    }

    // --- Métodos Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNroIdentificacao() {
        return nroIdentificacao;
    }

    public void setNroIdentificacao(String nroIdentificacao) {
        this.nroIdentificacao = nroIdentificacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // --- Métodos equals, hashCode e toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peca peca = (Peca) o;
        return Objects.equals(id, peca.id) && Objects.equals(nroIdentificacao, peca.nroIdentificacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nroIdentificacao);
    }

    @Override
    public String toString() {
        return "Peca{" +
                "id=" + id +
                ", nroIdentificacao='" + nroIdentificacao + '\'' +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}
