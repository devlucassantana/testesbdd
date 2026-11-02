package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.AlocacaoRequest;
import br.com.openinvestment.entity.AlocacaoRecurso;
import br.com.openinvestment.entity.ProjetoSocial;
import br.com.openinvestment.exception.BusinessException;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.AlocacaoRecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlocacaoService {

    private final AlocacaoRecursoRepository alocacaoRepository;
    private final ProjetoService projetoService;

    public List<AlocacaoRecurso> listarPorProjeto(Long idProjeto) {
        projetoService.findOrThrow(idProjeto); // valida existência
        return alocacaoRepository.findByProjetoIdProjeto(idProjeto);
    }

    @Transactional
    public AlocacaoRecurso alocar(AlocacaoRequest req) {
        ProjetoSocial projeto = projetoService.findOrThrow(req.idProjeto());

        // Regra: bloqueia se ultrapassar orçamento (equivale ao TRG_BLOQUEIA_ORCAMENTO_ESTOURADO)
        BigDecimal totalAtual = alocacaoRepository.sumValorByProjeto(req.idProjeto());
        BigDecimal novoTotal = totalAtual.add(req.valor());

        if (novoTotal.compareTo(projeto.getOrcamentoTotal()) > 0) {
            BigDecimal saldoRestante = projeto.getOrcamentoTotal().subtract(totalAtual);
            throw new BusinessException(
                    String.format("Alocação bloqueada. Saldo restante do projeto: R$ %.2f. " +
                            "Valor solicitado: R$ %.2f.", saldoRestante, req.valor()));
        }

        AlocacaoRecurso alocacao = AlocacaoRecurso.builder()
                .projeto(projeto)
                .descricao(req.descricao())
                .valor(req.valor())
                .tipoRecurso(req.tipoRecurso())
                .dataAlocacao(req.dataAlocacao())
                .build();
        alocacaoRepository.save(alocacao);

        // Atualiza verba e gera alerta se necessário (equivale ao TRG_CONTROLE_VERBA)
        projetoService.atualizarVerbaUtilizada(projeto, novoTotal);

        return alocacao;
    }

    public AlocacaoRecurso buscarPorId(Long id) {
        return alocacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alocação não encontrada com ID: " + id));
    }

    @Transactional
    public void excluir(Long id) {
        AlocacaoRecurso alocacao = buscarPorId(id);
        // Carrega o projeto antes do delete para evitar LazyInitializationException no proxy
        ProjetoSocial projeto = projetoService.findOrThrow(alocacao.getProjeto().getIdProjeto());
        alocacaoRepository.delete(alocacao);
        alocacaoRepository.flush(); // garante que o delete é executado antes do SUM
        BigDecimal novoTotal = alocacaoRepository.sumValorByProjeto(projeto.getIdProjeto());
        projetoService.atualizarVerbaUtilizada(projeto, novoTotal);
    }
}
