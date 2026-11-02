package br.com.openinvestment.dto.response;

import br.com.openinvestment.entity.ProjetoSocial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjetoResponse(
        Long idProjeto,
        String nomeProjeto,
        String descricao,
        String odsVinculacao,
        LocalDate dataInicio,
        LocalDate dataFimPrev,
        BigDecimal orcamentoTotal,
        BigDecimal verbaUtilizada,
        BigDecimal saldoDisponivel,
        Double percentualUtilizado,
        String statusProjeto,
        LocalDateTime criadoEm
) {
    public static ProjetoResponse from(ProjetoSocial p) {
        BigDecimal verba = p.getVerbaUtilizada() != null ? p.getVerbaUtilizada() : BigDecimal.ZERO;
        BigDecimal saldo = p.getOrcamentoTotal().subtract(verba);
        Double pct = verba
                .divide(p.getOrcamentoTotal(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        return new ProjetoResponse(
                p.getIdProjeto(), p.getNomeProjeto(), p.getDescricao(),
                p.getOdsVinculacao(), p.getDataInicio(), p.getDataFimPrev(),
                p.getOrcamentoTotal(), verba, saldo, pct,
                p.getStatusProjeto(), p.getCriadoEm()
        );
    }
}
