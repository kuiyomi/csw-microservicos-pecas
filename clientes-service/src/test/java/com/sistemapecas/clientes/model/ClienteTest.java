package com.sistemapecas.clientes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o modelo de domínio Cliente (equals, hashCode, toString, getters e setters).
 */
class ClienteTest {

    @Test
    @DisplayName("Deve verificar equals e hashCode para objetos iguais, diferentes e nulos")
    void equalsEHashCode_cenariosDiversos_comportamentoEsperado() {
        // Arrange
        Cliente c1 = new Cliente(1L, "12345678901", "Carlos Silva");
        Cliente c2 = new Cliente(1L, "12345678901", "Carlos Silva");
        Cliente cMesmoIdOutroCpf = new Cliente(1L, "99999999999", "Carlos Silva");
        Cliente cOutroIdMesmoCpf = new Cliente(2L, "12345678901", "Carlos Silva");
        Cliente cComIdNulo1 = new Cliente(null, "12345678901", "Carlos Silva");
        Cliente cComIdNulo2 = new Cliente(null, "12345678901", "Carlos Silva");

        // Act & Assert
        // Reflexividade
        assertEquals(c1, c1);
        // Simetria
        assertEquals(c1, c2);
        assertEquals(c2, c1);
        assertEquals(c1.hashCode(), c2.hashCode());

        // Nulidade e tipo diferente
        assertNotEquals(c1, null);
        assertNotEquals(c1, "outraString");

        // Diferença de atributos
        assertNotEquals(c1, cMesmoIdOutroCpf);
        assertNotEquals(c1, cOutroIdMesmoCpf);

        // Instâncias com ID nulo
        assertEquals(cComIdNulo1, cComIdNulo2);
        assertNotEquals(c1, cComIdNulo1);
    }

    @Test
    @DisplayName("Deve verificar getters, setters, construtores e toString")
    void gettersSettersEToString_dadosValidos_retornaValoresCorretos() {
        // Arrange
        Cliente cliente = new Cliente();

        // Act
        cliente.setId(5L);
        cliente.setCpf("98765432100");
        cliente.setNome("Mariana Costa");

        // Assert
        assertEquals(5L, cliente.getId());
        assertEquals("98765432100", cliente.getCpf());
        assertEquals("Mariana Costa", cliente.getNome());

        String str = cliente.toString();
        assertTrue(str.contains("98765432100"));
        assertTrue(str.contains("Mariana Costa"));
    }
}
