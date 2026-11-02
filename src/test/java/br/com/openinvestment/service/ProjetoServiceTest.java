package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.ProjetoRequest;
import br.com.openinvestment.dto.response.ProjetoResponse;
import br.com.openinvestment.entity.ProjetoSocial;
import br.com.openinvestment.exception.BusinessException;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.AlertaEsgRepository;
import br.com.openinvestment.repository.LogEsgRepository;
import br.com.openinvestment.repository.ProjetoSocialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock ProjetoSocialRepository projetoRepository;
    @Mock AlertaEsgRepository alertaRepository;
    @Mock LogEsgRepository logRepository;
    @InjectMocks ProjetoService projetoService;

    private ProjetoSocial projetoExemplo;

    @BeforeEach
    void setUp() {
        projetoExemplo = ProjetoSocial.builder()
                .idProjeto(1L)
                .nomeProjeto("Projeto Teste")
                .orcamentoTotal(new BigDecimal("100000.00"))
                .verbaUtilizada(BigDecimal.ZERO)
                .statusProjeto("PLANEJADO")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .build();
    }

    @Test
    @DisplayName("buscarPorId existente retorna ProjetoResponse")
    void buscarPorIdExistente() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoExemplo));

        ProjetoResponse resultado = projetoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nomeProjeto()).isEqualTo("Projeto Teste");
        assertThat(resultado.statusProjeto()).isEqualTo("PLANEJADO");
        assertThat(resultado.saldoDisponivel()).isEqualByComparingTo(new BigDecimal("100000.00"));
    }

    @Test
    @DisplayName("buscarPorId inexistente lança ResourceNotFoundException")
    void buscarPorIdInexistente() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("criar salva projeto com status PLANEJADO")
    void criarProjeto() {
        when(projetoRepository.save(any())).thenReturn(projetoExemplo);
        when(logRepository.save(any())).thenReturn(null);

        ProjetoRequest req = new ProjetoRequest(
                "Projeto Teste", "Descricao", "ODS 4",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100000.00")
        );

        ProjetoResponse resultado = projetoService.criar(req);

        assertThat(resultado).isNotNull();
        verify(projetoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("atualizarStatus com status inválido lança BusinessException")
    void atualizarStatusInvalido() {
        // A validação do status ocorre antes de qualquer acesso ao repositório
        assertThatThrownBy(() -> projetoService.atualizarStatus(1L, "STATUS_INVALIDO"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("STATUS_INVALIDO");
    }

    @Test
    @DisplayName("atualizarStatus PLANEJADO para EM_ANDAMENTO gera alerta")
    void atualizarStatusParaEmAndamento() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoExemplo));
        when(projetoRepository.save(any())).thenReturn(projetoExemplo);
        when(alertaRepository.save(any())).thenReturn(null);
        when(logRepository.save(any())).thenReturn(null);

        projetoService.atualizarStatus(1L, "EM_ANDAMENTO");

        verify(alertaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("iniciarSeNecessario muda PLANEJADO para EM_ANDAMENTO")
    void iniciarSeNecessario() {
        when(projetoRepository.save(any())).thenReturn(projetoExemplo);
        when(alertaRepository.save(any())).thenReturn(null);
        when(logRepository.save(any())).thenReturn(null);

        projetoService.iniciarSeNecessario(projetoExemplo);

        assertThat(projetoExemplo.getStatusProjeto()).isEqualTo("EM_ANDAMENTO");
        verify(projetoRepository, times(1)).save(projetoExemplo);
    }

    @Test
    @DisplayName("iniciarSeNecessario NÃO altera projeto já EM_ANDAMENTO")
    void iniciarSeNecessarioJaEmAndamento() {
        projetoExemplo.setStatusProjeto("EM_ANDAMENTO");

        projetoService.iniciarSeNecessario(projetoExemplo);

        verify(projetoRepository, never()).save(any());
    }
}
