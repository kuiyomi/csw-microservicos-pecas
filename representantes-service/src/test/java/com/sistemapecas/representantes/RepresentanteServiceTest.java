package com.sistemapecas.representantes;

import com.sistemapecas.representantes.exception.DuplicateCpfException;
import com.sistemapecas.representantes.exception.ResourceNotFoundException;
import com.sistemapecas.representantes.model.Representante;
import com.sistemapecas.representantes.repository.RepresentanteRepository;
import com.sistemapecas.representantes.service.RepresentanteService;
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
 * Testes unitários puros para a camada de serviço (RepresentanteService) utilizando Mockito e JUnit 5.
 * Segue as práticas descritas no guia de testes (testes__2_.pdf):
 *  - Isolamento completo sem carregar o contexto Spring (@ExtendWith(MockitoExtension.class)).
 *  - Mocks para colaboradores externos (@Mock RepresentanteRepository).
 *  - Padrão Arrange-Act-Assert (AAA) com comentários explícitos.
 *  - Nomes de métodos no padrão metodoTestado_cenario_comportamentoEsperado e @DisplayName em português.
 *  - Verificação de chamadas com verify() e VerificationMode (times(1), never()).
 *  - Testes de casos limite para campos nulos, vazios e com espaços.
 */
@ExtendWith(MockitoExtension.class)
class RepresentanteServiceTest {

    @Mock
    private RepresentanteRepository representanteRepository;

    private MeterRegistry meterRegistry;
    private RepresentanteService representanteService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        representanteService = new RepresentanteService(representanteRepository, meterRegistry);
    }

    // -------------------------------------------------------------------------
    // cadastrar()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve cadastrar um representante com sucesso quando os dados forem válidos")
    void cadastrar_quandoRepresentanteValido_salvaEIncrementaMetrica() {
        // Arrange
        Representante rep = new Representante("12345678901", "Juliana Mendes");
        when(representanteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(representanteRepository.save(any(Representante.class))).thenAnswer(invocation -> {
            Representante r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        Representante cadastrado = representanteService.cadastrar(rep);

        // Assert
        assertNotNull(cadastrado.getId(), "O ID do representante deve ser gerado");
        assertEquals("12345678901", cadastrado.getCpf());
        assertEquals("Juliana Mendes", cadastrado.getNome());
        assertEquals(1.0, meterRegistry.get("representantes.cadastrados.total").counter().count(),
                "O contador de métricas deve ser incrementado em 1");
        verify(representanteRepository, times(1)).existsByCpf("12345678901");
        verify(representanteRepository, times(1)).save(any(Representante.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o objeto representante for nulo")
    void cadastrar_quandoObjetoRepresentanteNulo_lancaIllegalArgumentException() {
        // Arrange
        Representante repNulo = null;

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(repNulo);
        });

        // Assert
        assertEquals("Dados do representante não podem ser nulos", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF for nulo")
    void cadastrar_quandoCpfNulo_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante(null, "Juliana Mendes");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF for string vazia")
    void cadastrar_quandoCpfVazio_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante("", "Juliana Mendes");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF contiver apenas espaços")
    void cadastrar_quandoCpfSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante("   ", "Juliana Mendes");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O CPF é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for nulo")
    void cadastrar_quandoNomeNulo_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante("12345678901", null);

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome for string vazia")
    void cadastrar_quandoNomeVazio_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante("12345678901", "");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome contiver apenas espaços em branco")
    void cadastrar_quandoNomeSomenteEspacos_lancaIllegalArgumentException() {
        // Arrange
        Representante rep = new Representante("12345678901", "   ");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertEquals("O nome é obrigatório", exception.getMessage());
        verify(representanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DuplicateCpfException quando CPF já estiver cadastrado")
    void cadastrar_quandoCpfJaExiste_lancaDuplicateCpfException() {
        // Arrange
        Representante rep = new Representante("11122233344", "Lucas Almeida");
        when(representanteRepository.existsByCpf("11122233344")).thenReturn(true);

        // Act
        DuplicateCpfException exception = assertThrows(DuplicateCpfException.class, () -> {
            representanteService.cadastrar(rep);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Já existe um representante cadastrado com o CPF: 11122233344"));
        verify(representanteRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listarTodos()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar lista de representantes cadastrados")
    void listarTodos_quandoChamado_retornaListaDeRepresentantes() {
        // Arrange
        List<Representante> listaMock = List.of(
                new Representante("11111111111", "Camila Rocha"),
                new Representante("22222222222", "Marcos Silva")
        );
        when(representanteRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Representante> resultado = representanteService.listarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(representanteRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // buscarPorId()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar representante quando o ID existir")
    void buscarPorId_quandoIdExiste_retornaRepresentante() {
        // Arrange
        Representante repMock = new Representante("33333333333", "Thiago Pereira");
        repMock.setId(1L);
        when(representanteRepository.findById(1L)).thenReturn(Optional.of(repMock));

        // Act
        Representante encontrado = representanteService.buscarPorId(1L);

        // Assert
        assertNotNull(encontrado);
        assertEquals(1L, encontrado.getId());
        assertEquals("33333333333", encontrado.getCpf());
        verify(representanteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existir")
    void buscarPorId_quandoIdNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(representanteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            representanteService.buscarPorId(99L);
        });

        // Assert
        assertEquals("Representante não encontrado com o ID: 99", exception.getMessage());
        verify(representanteRepository, times(1)).findById(99L);
    }

    // -------------------------------------------------------------------------
    // buscarPorNome()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar representantes que contêm o termo buscado de forma case-insensitive")
    void buscarPorNome_quandoTermoInformado_retornaRepresentantesCorrespondentes() {
        // Arrange
        List<Representante> listaMock = List.of(new Representante("44444444444", "Fernanda Santos"));
        when(representanteRepository.findByNomeContainingIgnoreCase("fernanda")).thenReturn(listaMock);

        // Act
        List<Representante> resultado = representanteService.buscarPorNome("fernanda");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Fernanda Santos", resultado.get(0).getNome());
        verify(representanteRepository, times(1)).findByNomeContainingIgnoreCase("fernanda");
    }

    // -------------------------------------------------------------------------
    // buscarPorCpf()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar representante quando o CPF existir")
    void buscarPorCpf_quandoExiste_retornaRepresentante() {
        // Arrange
        Representante repMock = new Representante("77788899900", "Eduardo Paes");
        when(representanteRepository.findByCpf("77788899900")).thenReturn(Optional.of(repMock));

        // Act
        Representante encontrado = representanteService.buscarPorCpf("77788899900");

        // Assert
        assertNotNull(encontrado);
        assertEquals("77788899900", encontrado.getCpf());
        verify(representanteRepository, times(1)).findByCpf("77788899900");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o CPF não existir")
    void buscarPorCpf_quandoNaoExiste_lancaResourceNotFoundException() {
        // Arrange
        when(representanteRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            representanteService.buscarPorCpf("00000000000");
        });

        // Assert
        assertEquals("Representante não encontrado com o CPF: 00000000000", exception.getMessage());
        verify(representanteRepository, times(1)).findByCpf("00000000000");
    }
}
