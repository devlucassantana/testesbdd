package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.AlocacaoRequest;
import br.com.openinvestment.entity.AlocacaoRecurso;
import br.com.openinvestment.entity.ProjetoSocial;
import br.com.openinvestment.exception.BusinessException;
import br.com.openinvestment.repository.AlocacaoRecursoRepository;
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
class AlocacaoServiceTest {

    @Mock AlocacaoRecursoRepository alocacaoRepository;
    @Mock ProjetoService projetoService;
    @InjectMocks AlocacaoService alocacaoService;

    private ProjetoSocial projeto;

    @BeforeEach
    void setUp() {
        projeto = ProjetoSocial.builder()
                .idProjeto(1L)
                .nomeProjeto("Projeto ESG")
                .orcamentoTotal(new BigDecimal("100000.00"))
                .verbaUtilizada(BigDecimal.ZERO)
                .statusProjeto("EM_ANDAMENTO")
                .dataInicio(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("alocar dentro do orçamento salva e retorna alocação")
    void alocarDentroOrcamento() {
        when(projetoService.findOrThrow(1L)).thenReturn(projeto);
        when(alocacaoRepository.sumValorByProjeto(1L)).thenReturn(BigDecimal.ZERO);
        when(alocacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(projetoService).atualizarVerbaUtilizada(any(), any());

        AlocacaoRequest req = new AlocacaoRequest(1L, "Equipamentos", new BigDecimal("50000.00"), "MATERIAL", null);
        AlocacaoRecurso result = alocacaoService.alocar(req);

        assertThat(result).isNotNull();
        assertThat(result.getValor()).isEqualByComparingTo(new BigDecimal("50000.00"));
        verify(alocacaoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("alocar acima do orçamento lança BusinessException com saldo")
    void alocarAcimaOrcamento() {
        when(projetoService.findOrThrow(1L)).thenReturn(projeto);
        when(alocacaoRepository.sumValorByProjeto(1L)).thenReturn(new BigDecimal("90000.00"));

        AlocacaoRequest req = new AlocacaoRequest(1L, "Excede", new BigDecimal("20000.00"), "FINANCEIRO", null);

        assertThatThrownBy(() -> alocacaoService.alocar(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("10000"); // saldo restante = 10000
    }

    @Test
    @DisplayName("alocar exatamente no limite é permitido")
    void alocarExatamenteNoLimite() {
        when(projetoService.findOrThrow(1L)).thenReturn(projeto);
        when(alocacaoRepository.sumValorByProjeto(1L)).thenReturn(BigDecimal.ZERO);
        when(alocacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(projetoService).atualizarVerbaUtilizada(any(), any());

        AlocacaoRequest req = new AlocacaoRequest(1L, "Total", new BigDecimal("100000.00"), "FINANCEIRO", null);

        assertThatNoException().isThrownBy(() -> alocacaoService.alocar(req));
    }
}
