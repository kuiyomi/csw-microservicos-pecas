package com.sistemapecas.clientes;

import com.sistemapecas.clientes.exception.DuplicateCpfException;
import com.sistemapecas.clientes.exception.ResourceNotFoundException;
import com.sistemapecas.clientes.model.Cliente;
import com.sistemapecas.clientes.repository.ClienteRepository;
import com.sistemapecas.clientes.service.ClienteService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários puros para a camada de serviço (ClienteService) utilizando Mockito e JUnit 5.
 * Segue as práticas descritas no guia de testes (testes__2_.pdf):
 *  - Isolamento completo sem carregar o contexto Spring (@ExtendWith(MockitoExtension.class)).
 *  - Mocks para colaboradores externos (@Mock ClienteRepository).
 *  - Padrão Arrange-Act-Assert (AAA) com comentários explícitos.
 *  - Nomes de métodos no padrão metodoTestado_cenario_comportamentoEsperado e @DisplayName em português.
 *  - Verificação de chamadas com verify() e VerificationMode (times(1), never()).
 *  - Testes de casos limite para campos nulos, vazios e com espaços.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private MeterRegistry meterRegistry;
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        clienteService = new ClienteService(clienteRepository, meterRegistry);
    }

    // -------------------------------------------------------------------------
    // cadastrar()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve cadastrar um cliente com sucesso quando os dados forem válidos")
    void cadastrar_quandoClienteValido_salvaEIncrementaMetrica() {
        // Arrange
        Cliente cliente = new Cliente("12345678901", "Lucas Pereira");
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        // Act
        Cliente cadastrado = clienteService.cadastrar(cliente);

        // Assert
        assertNotNull(cadastrado.getId(), "O ID do cliente deve ser gerado");
        assertEquals("12345678901", cadastrado.getCpf());
        assertEquals("Lucas Pereira", cadastrado.getNome());
        assertEquals(1.0, meterRegistry.get("clientes.cadastrados.total").counter().count(),
                "O contador de métricas deve ser incrementado em 1");
        verify(clienteRepository, times(1)).existsByCpf("12345678901");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve redefinir o ID para null antes de salvar caso o objeto já possua ID preenchido")
    void cadastrar_quandoObjetoPossuiIdPreenchido_redefineIdParaNull() {
        // Arrange
        Cliente clienteComId = new Cliente("12345678901", "Lucas Pereira");
        clienteComId.setId(999L);
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            assertNull(c.getId(), "O ID deve ser redefinido para null antes de salvar no banco");
            c.setId(10L);
            return c;
        });

        // Act
        Cliente salvo = clienteService.cadastrar(clienteComId);

        // Assert
        assertEquals(10L, salvo.getId());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o objeto cliente for nulo")
    void cadastrar_quandoObjetoClienteNulo_lancaIllegalArgumentException() {
        // Arrange
        Cliente clienteNulo = null;

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(clienteNulo);
        });

        // Assert
        assertEquals("Dados do cliente não podem ser nulos", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF for nulo")
    void cadastrar_quandoCpfNulo_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente(null, "Lucas Pereira");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF for string vazia")
    void cadastrar_quandoCpfVazio_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente("", "Lucas Pereira");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF contiver apenas espaços")
    void cadastrar_quandoCpfSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente("   ", "Lucas Pereira");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for nulo")
    void cadastrar_quandoNomeNulo_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente("12345678901", null);

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for string vazia")
    void cadastrar_quandoNomeVazio_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente("12345678901", "");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome contiver apenas espaços em branco")
    void cadastrar_quandoNomeSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Cliente cliente = new Cliente("12345678901", "   ");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DuplicateCpfException quando CPF já estiver cadastrado")
    void cadastrar_quandoCpfJaExiste_lancaDuplicateCpfException() {
        // Arrange
        Cliente cliente = new Cliente("11122233344", "João da Silva");
        when(clienteRepository.existsByCpf("11122233344")).thenReturn(true);

        // Act
        DuplicateCpfException exception = assertThrows(DuplicateCpfException.class, () -> {
            clienteService.cadastrar(cliente);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Já existe um cliente cadastrado com o CPF: 11122233344"));
        verify(clienteRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listarTodos()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar lista de clientes cadastrados")
    void listarTodos_quandoChamado_retornaListaDeClientes() {
        // Arrange
        List<Cliente> listaMock = List.of(
                new Cliente("11111111111", "Ana Santos"),
                new Cliente("22222222222", "Bruno Lima")
        );
        when(clienteRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(clienteRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // buscarPorId()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar cliente quando o ID existir")
    void buscarPorId_quandoIdExiste_retornaCliente() {
        // Arrange
        Cliente clienteMock = new Cliente("33333333333", "Claudia Ramos");
        clienteMock.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));

        // Act
        Cliente encontrado = clienteService.buscarPorId(1L);

        // Assert
        assertNotNull(encontrado);
        assertEquals(1L, encontrado.getId());
        assertEquals("33333333333", encontrado.getCpf());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existir")
    void buscarPorId_quandoIdNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.buscarPorId(99L);
        });

        // Assert
        assertEquals("Cliente não encontrado com o ID: 99", exception.getMessage());
        verify(clienteRepository, times(1)).findById(99L);
    }

    // -------------------------------------------------------------------------
    // buscarPorNome()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar clientes que contêm o termo buscado de forma case-insensitive")
    void buscarPorNome_quandoTermoInformado_retornaClientesCorrespondentes() {
        // Arrange
        List<Cliente> listaMock = List.of(new Cliente("44444444444", "Fernanda Oliveira"));
        when(clienteRepository.findByNomeContainingIgnoreCase("fernanda")).thenReturn(listaMock);

        // Act
        List<Cliente> resultado = clienteService.buscarPorNome("fernanda");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Fernanda Oliveira", resultado.get(0).getNome());
        verify(clienteRepository, times(1)).findByNomeContainingIgnoreCase("fernanda");
    }

    // -------------------------------------------------------------------------
    // buscarPorCpf()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar cliente quando o CPF existir")
    void buscarPorCpf_quandoExiste_retornaCliente() {
        // Arrange
        Cliente clienteMock = new Cliente("77788899900", "Diego Martins");
        when(clienteRepository.findByCpf("77788899900")).thenReturn(Optional.of(clienteMock));

        // Act
        Cliente encontrado = clienteService.buscarPorCpf("77788899900");

        // Assert
        assertNotNull(encontrado);
        assertEquals("77788899900", encontrado.getCpf());
        verify(clienteRepository, times(1)).findByCpf("77788899900");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o CPF não existir")
    void buscarPorCpf_quandoNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(clienteRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.buscarPorCpf("00000000000");
        });

        // Assert
        assertEquals("Cliente não encontrado com o CPF: 00000000000", exception.getMessage());
        verify(clienteRepository, times(1)).findByCpf("00000000000");
    }
}
