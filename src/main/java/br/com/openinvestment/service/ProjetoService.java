package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.ProjetoRequest;
import br.com.openinvestment.dto.response.ProjetoResponse;
import br.com.openinvestment.entity.AlertaEsg;
import br.com.openinvestment.entity.LogEsg;
import br.com.openinvestment.entity.ProjetoSocial;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.AlertaEsgRepository;
import br.com.openinvestment.repository.LogEsgRepository;
import br.com.openinvestment.repository.ProjetoSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoSocialRepository projetoRepository;
    private final AlertaEsgRepository alertaRepository;
    private final LogEsgRepository logRepository;

    public List<ProjetoResponse> listarTodos() {
        return projetoRepository.findAll().stream().map(ProjetoResponse::from).toList();
    }

    public List<ProjetoResponse> listarPorStatus(String status) {
        return projetoRepository.findByStatusProjeto(status.toUpperCase())
                .stream().map(ProjetoResponse::from).toList();
    }

    public List<ProjetoResponse> listarOrcamentoCritico() {
        return projetoRepository.findProjetosComOrcamentoCritico()
                .stream().map(ProjetoResponse::from).toList();
    }

    public ProjetoResponse buscarPorId(Long id) {
        return ProjetoResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProjetoResponse criar(ProjetoRequest req) {
        ProjetoSocial projeto = ProjetoSocial.builder()
                .nomeProjeto(req.nomeProjeto())
                .descricao(req.descricao())
                .odsVinculacao(req.odsVinculacao())
                .dataInicio(req.dataInicio())
                .dataFimPrev(req.dataFimPrev())
                .orcamentoTotal(req.orcamentoTotal())
                .build();
        projetoRepository.save(projeto);

        registrarLog("projeto_social", "INSERT", projeto.getIdProjeto(),
                "Projeto criado: " + projeto.getNomeProjeto());
        return ProjetoResponse.from(projeto);
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest req) {
        ProjetoSocial projeto = findOrThrow(id);
        projeto.setNomeProjeto(req.nomeProjeto());
        projeto.setDescricao(req.descricao());
        projeto.setOdsVinculacao(req.odsVinculacao());
        projeto.setDataInicio(req.dataInicio());
        projeto.setDataFimPrev(req.dataFimPrev());
        projeto.setOrcamentoTotal(req.orcamentoTotal());
        projetoRepository.save(projeto);

        registrarLog("projeto_social", "UPDATE", projeto.getIdProjeto(),
                "Projeto atualizado: " + projeto.getNomeProjeto());
        return ProjetoResponse.from(projeto);
    }

    private static final java.util.Set<String> STATUS_VALIDOS =
            java.util.Set.of("PLANEJADO", "EM_ANDAMENTO", "CONCLUIDO", "CANCELADO");

    @Transactional
    public void atualizarStatus(Long id, String novoStatus) {
        String statusUpper = novoStatus.toUpperCase();
        if (!STATUS_VALIDOS.contains(statusUpper)) {
            throw new br.com.openinvestment.exception.BusinessException(
                    "Status inválido: '" + novoStatus + "'. Valores aceitos: " + STATUS_VALIDOS);
        }
        ProjetoSocial projeto = findOrThrow(id);
        String statusAnterior = projeto.getStatusProjeto();
        projeto.setStatusProjeto(statusUpper);
        projetoRepository.save(projeto);

        registrarLog("projeto_social", "STATUS_UPDATE", id,
                "Status alterado de " + statusAnterior + " para " + statusUpper);

        if ("EM_ANDAMENTO".equals(statusUpper)) {
            gerarAlerta(projeto, "PROJETO_INICIADO",
                    "Projeto '" + projeto.getNomeProjeto() + "' iniciado manualmente.");
        }
    }

    @Transactional
    public void excluir(Long id) {
        ProjetoSocial projeto = findOrThrow(id);
        try {
            registrarLog("projeto_social", "DELETE", id,
                    "Projeto excluído: " + projeto.getNomeProjeto());
            projetoRepository.delete(projeto);
            projetoRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new br.com.openinvestment.exception.BusinessException(
                    "Não é possível excluir o projeto '" + projeto.getNomeProjeto() +
                    "' pois existem alocações, impactos ou alertas vinculados. " +
                    "Remova os registros dependentes antes de excluir o projeto.");
        }
    }

    // Chamado por AlocacaoService após salvar alocação
    @Transactional
    public void atualizarVerbaUtilizada(ProjetoSocial projeto, java.math.BigDecimal totalAlocado) {
        projeto.setVerbaUtilizada(totalAlocado);
        projetoRepository.save(projeto);

        double pct = totalAlocado
                .divide(projeto.getOrcamentoTotal(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();

        if (pct >= 80) {
            // Evita alertas duplicados: só cria se não houver ORCAMENTO_CRITICO pendente
            boolean jaTemAlertaPendente = alertaRepository
                    .findByProjetoIdProjetoOrderByDataAlertaDesc(projeto.getIdProjeto())
                    .stream()
                    .anyMatch(a -> "ORCAMENTO_CRITICO".equals(a.getTipoAlerta())
                            && "PENDENTE".equals(a.getStatusAlerta()));

            if (!jaTemAlertaPendente) {
                gerarAlerta(projeto, "ORCAMENTO_CRITICO",
                        String.format("Orçamento do projeto '%s' atingiu %.1f%% do total disponível.",
                                projeto.getNomeProjeto(), pct));
            }
        }
    }

    // Chamado por ImpactoService para transição automática PLANEJADO → EM_ANDAMENTO
    @Transactional
    public void iniciarSeNecessario(ProjetoSocial projeto) {
        if ("PLANEJADO".equals(projeto.getStatusProjeto())) {
            projeto.setStatusProjeto("EM_ANDAMENTO");
            projetoRepository.save(projeto);
            gerarAlerta(projeto, "PROJETO_INICIADO",
                    "Projeto '" + projeto.getNomeProjeto() + "' iniciado automaticamente ao registrar primeiro impacto social.");
            registrarLog("projeto_social", "AUTO_STATUS", projeto.getIdProjeto(),
                    "Status alterado automaticamente para EM_ANDAMENTO.");
        }
    }

    private void gerarAlerta(ProjetoSocial projeto, String tipo, String mensagem) {
        AlertaEsg alerta = AlertaEsg.builder()
                .projeto(projeto)
                .tipoAlerta(tipo)
                .mensagem(mensagem)
                .build();
        alertaRepository.save(alerta);
    }

    private void registrarLog(String tabela, String operacao, Long refId, String descricao) {
        LogEsg log = LogEsg.builder()
                .tabelaOrigem(tabela)
                .operacao(operacao)
                .idReferencia(refId)
                .descricao(descricao)
                .build();
        logRepository.save(log);
    }

    public ProjetoSocial findOrThrow(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + id));
    }
}
