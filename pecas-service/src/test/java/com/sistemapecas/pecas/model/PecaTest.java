package com.sistemapecas.pecas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o modelo de domínio Peca (equals, hashCode, toString, getters e setters).
 */
class PecaTest {

    @Test
    @DisplayName("Deve verificar equals e hashCode para objetos iguais, diferentes e nulos")
    void equalsEHashCode_cenariosDiversos_comportamentoEsperado() {
        // Arrange
        Peca peca1 = new Peca(1L, "PEC-001", "Amortecedor", "Dianteiro");
        Peca peca2 = new Peca(1L, "PEC-001", "Amortecedor", "Dianteiro");
        Peca pecaMesmoIdOutroCod = new Peca(1L, "PEC-002", "Amortecedor", "Dianteiro");
        Peca pecaOutroIdMesmoCod = new Peca(2L, "PEC-001", "Amortecedor", "Dianteiro");
        Peca pecaComIdNulo1 = new Peca(null, "PEC-001", "Amortecedor", "Dianteiro");
        Peca pecaComIdNulo2 = new Peca(null, "PEC-001", "Amortecedor", "Dianteiro");

        // Act & Assert
        // Reflexividade
        assertEquals(peca1, peca1);
        // Simetria
        assertEquals(peca1, peca2);
        assertEquals(peca2, peca1);
        assertEquals(peca1.hashCode(), peca2.hashCode());

        // Nulidade e tipo diferente
        assertNotEquals(peca1, null);
        assertNotEquals(peca1, "outraString");

        // Diferença de atributos
        assertNotEquals(peca1, pecaMesmoIdOutroCod);
        assertNotEquals(peca1, pecaOutroIdMesmoCod);

        // Instâncias com ID nulo
        assertEquals(pecaComIdNulo1, pecaComIdNulo2);
        assertNotEquals(peca1, pecaComIdNulo1);
    }

    @Test
    @DisplayName("Deve verificar getters, setters, construtores e toString")
    void gettersSettersEToString_dadosValidos_retornaValoresCorretos() {
        // Arrange
        Peca peca = new Peca();

        // Act
        peca.setId(5L);
        peca.setNroIdentificacao("PEC-123");
        peca.setNome("Disco de Freio");
        peca.setDescricao("Ventilado");

        // Assert
        assertEquals(5L, peca.getId());
        assertEquals("PEC-123", peca.getNroIdentificacao());
        assertEquals("Disco de Freio", peca.getNome());
        assertEquals("Ventilado", peca.getDescricao());

        String str = peca.toString();
        assertTrue(str.contains("PEC-123"));
        assertTrue(str.contains("Disco de Freio"));
    }
}
