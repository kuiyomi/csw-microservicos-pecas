package com.sistemapecas.pecas;

import com.sistemapecas.pecas.exception.DuplicateIdentificationException;
import com.sistemapecas.pecas.exception.ResourceNotFoundException;
import com.sistemapecas.pecas.model.Peca;
import com.sistemapecas.pecas.repository.PecaRepository;
import com.sistemapecas.pecas.service.PecaService;
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
 * Testes unitários puros para a camada de serviço (PecaService) utilizando Mockito e JUnit 5.
 * Segue as práticas descritas no guia de testes (testes__2_.pdf):
 *  - Isolamento completo sem carregar o contexto Spring (@ExtendWith(MockitoExtension.class)).
 *  - Mocks para colaboradores externos (@Mock PecaRepository).
 *  - Padrão Arrange-Act-Assert (AAA) com comentários explícitos.
 *  - Nomes de métodos no padrão metodoTestado_cenario_comportamentoEsperado e @DisplayName em português.
 *  - Verificação de chamadas com verify() e VerificationMode (times(1), never()).
 *  - Testes de casos limite para campos nulos, vazios e com espaços.
 */
@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    private MeterRegistry meterRegistry;
    private PecaService pecaService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        pecaService = new PecaService(pecaRepository, meterRegistry);
    }

    // -------------------------------------------------------------------------
    // cadastrar()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve cadastrar uma peça com sucesso quando os dados forem válidos")
    void cadastrar_quandoPecaValida_salvaEIncrementaMetrica() {
        // Arrange
        Peca peca = new Peca("PEC-100", "Filtro de Ar", "Filtro de motor");
        when(pecaRepository.existsByNroIdentificacao("PEC-100")).thenReturn(false);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(invocation -> {
            Peca p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Act
        Peca cadastrada = pecaService.cadastrar(peca);

        // Assert
        assertNotNull(cadastrada.getId(), "O ID da peça deve ser gerado");
        assertEquals("PEC-100", cadastrada.getNroIdentificacao());
        assertEquals("Filtro de Ar", cadastrada.getNome());
        assertEquals(1.0, meterRegistry.get("pecas.cadastradas.total").counter().count(),
                "O contador de métricas deve ser incrementado em 1");
        verify(pecaRepository, times(1)).existsByNroIdentificacao("PEC-100");
        verify(pecaRepository, times(1)).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o objeto peça for nulo")
    void cadastrar_quandoObjetoPecaNulo_lancaIllegalArgumentException() {
        // Arrange
        Peca pecaNula = null;

        // Act & Assert
        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(pecaNula);
        });

        // Assert
        assertEquals("Dados da peça não podem ser nulos", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nroIdentificacao for nulo")
    void cadastrar_quandoNroIdentificacaoNulo_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca(null, "Pastilha de Freio", "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O número de identificação é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nroIdentificacao for string vazia")
    void cadastrar_quandoNroIdentificacaoVazio_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca("", "Pastilha de Freio", "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O número de identificação é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nroIdentificacao contiver apenas espaços")
    void cadastrar_quandoNroIdentificacaoSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca("   ", "Pastilha de Freio", "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O número de identificação é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for nulo")
    void cadastrar_quandoNomeNulo_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca("PEC-200", null, "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for string vazia")
    void cadastrar_quandoNomeVazio_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca("PEC-200", "", "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome contiver apenas espaços em branco")
    void cadastrar_quandoNomeSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Peca peca = new Peca("PEC-200", "   ", "Descrição");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(pecaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DuplicateIdentificationException quando nroIdentificacao já estiver cadastrado")
    void cadastrar_quandoNroIdentificacaoJaExiste_lancaDuplicateIdentificationException() {
        // Arrange
        Peca peca = new Peca("PEC-DUP", "Radiador", "Radiador de cobre");
        when(pecaRepository.existsByNroIdentificacao("PEC-DUP")).thenReturn(true);

        // Act
        DuplicateIdentificationException exception = assertThrows(DuplicateIdentificationException.class, () -> {
            pecaService.cadastrar(peca);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Já existe uma peça cadastrada com o número de identificação: PEC-DUP"));
        verify(pecaRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listarTodas()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar lista de peças cadastradas")
    void listarTodas_quandoChamado_retornaListaDePecas() {
        // Arrange
        List<Peca> listaMock = List.of(
                new Peca("PEC-01", "Amortecedor", "Dianteiro"),
                new Peca("PEC-02", "Mola", "Traseira")
        );
        when(pecaRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Peca> resultado = pecaService.listarTodas();

        // Assert
        assertEquals(2, resultado.size());
        verify(pecaRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // buscarPorId()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar peça quando o ID existir")
    void buscarPorId_quandoIdExiste_retornaPeca() {
        // Arrange
        Peca pecaMock = new Peca("PEC-01", "Amortecedor", "Dianteiro");
        pecaMock.setId(1L);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(pecaMock));

        // Act
        Peca encontrada = pecaService.buscarPorId(1L);

        // Assert
        assertNotNull(encontrada);
        assertEquals(1L, encontrada.getId());
        assertEquals("PEC-01", encontrada.getNroIdentificacao());
        verify(pecaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existir")
    void buscarPorId_quandoIdNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            pecaService.buscarPorId(99L);
        });

        // Assert
        assertEquals("Peça não encontrada com o ID: 99", exception.getMessage());
        verify(pecaRepository, times(1)).findById(99L);
    }

    // -------------------------------------------------------------------------
    // buscarPorNome()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar peças que contêm o termo buscado de forma case-insensitive")
    void buscarPorNome_quandoTermoInformado_retornaPecasCorrespondentes() {
        // Arrange
        List<Peca> listaMock = List.of(new Peca("PEC-01", "Bateria Automotiva", "Desc"));
        when(pecaRepository.findByNomeContainingIgnoreCase("bateria")).thenReturn(listaMock);

        // Act
        List<Peca> resultado = pecaService.buscarPorNome("bateria");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Bateria Automotiva", resultado.get(0).getNome());
        verify(pecaRepository, times(1)).findByNomeContainingIgnoreCase("bateria");
    }

    // -------------------------------------------------------------------------
    // buscarPorNroIdentificacao()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar peça quando o número de identificação existir")
    void buscarPorNroIdentificacao_quandoExiste_retornaPeca() {
        // Arrange
        Peca pecaMock = new Peca("PEC-EXATO", "Vela", "Vela especial");
        when(pecaRepository.findByNroIdentificacao("PEC-EXATO")).thenReturn(Optional.of(pecaMock));

        // Act
        Peca encontrada = pecaService.buscarPorNroIdentificacao("PEC-EXATO");

        // Assert
        assertNotNull(encontrada);
        assertEquals("PEC-EXATO", encontrada.getNroIdentificacao());
        verify(pecaRepository, times(1)).findByNroIdentificacao("PEC-EXATO");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o número de identificação não existir")
    void buscarPorNroIdentificacao_quandoNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(pecaRepository.findByNroIdentificacao("INEXISTENTE")).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            pecaService.buscarPorNroIdentificacao("INEXISTENTE");
        });

        // Assert
        assertEquals("Peça não encontrada com o número de identificação: INEXISTENTE", exception.getMessage());
        verify(pecaRepository, times(1)).findByNroIdentificacao("INEXISTENTE");
    }
}
