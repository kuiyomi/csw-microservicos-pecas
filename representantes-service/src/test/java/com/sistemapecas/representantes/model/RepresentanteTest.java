package com.sistemapecas.representantes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o modelo de domínio Representante (equals, hashCode, toString, getters e setters).
 */
class RepresentanteTest {

    @Test
    @DisplayName("Deve verificar equals e hashCode para objetos iguais, diferentes e nulos")
    void equalsEHashCode_cenariosDiversos_comportamentoEsperado() {
        // Arrange
        Representante r1 = new Representante(1L, "12345678901", "Roberto Dias");
        Representante r2 = new Representante(1L, "12345678901", "Roberto Dias");
        Representante rMesmoIdOutroCpf = new Representante(1L, "99999999999", "Roberto Dias");
        Representante rOutroIdMesmoCpf = new Representante(2L, "12345678901", "Roberto Dias");
        Representante rComIdNulo1 = new Representante(null, "12345678901", "Roberto Dias");
        Representante rComIdNulo2 = new Representante(null, "12345678901", "Roberto Dias");

        // Act & Assert
        // Reflexividade
        assertEquals(r1, r1);
        // Simetria
        assertEquals(r1, r2);
        assertEquals(r2, r1);
        assertEquals(r1.hashCode(), r2.hashCode());

        // Nulidade e tipo diferente
        assertNotEquals(r1, null);
        assertNotEquals(r1, "outraString");

        // Diferença de atributos
        assertNotEquals(r1, rMesmoIdOutroCpf);
        assertNotEquals(r1, rOutroIdMesmoCpf);

        // Instâncias com ID nulo
        assertEquals(rComIdNulo1, rComIdNulo2);
        assertNotEquals(r1, rComIdNulo1);
    }

    @Test
    @DisplayName("Deve verificar getters, setters, construtores e toString")
    void gettersSettersEToString_dadosValidos_retornaValoresCorretos() {
        // Arrange
        Representante rep = new Representante();

        // Act
        rep.setId(5L);
        rep.setCpf("98765432100");
        rep.setNome("Juliana Mendes");

        // Assert
        assertEquals(5L, rep.getId());
        assertEquals("98765432100", rep.getCpf());
        assertEquals("Juliana Mendes", rep.getNome());

        String str = rep.toString();
        assertTrue(str.contains("98765432100"));
        assertTrue(str.contains("Juliana Mendes"));
    }
}
