package br.com.openinvestment.dto.request;

import jakarta.validation.constraints.*;

public record ImpactoRequest(
        @NotNull Long idProjeto,
        @NotNull Long idBeneficiario,

        @NotBlank
        @Pattern(regexp = "CAPACITACAO|DOACAO|ATENDIMENTO|EMPREGO|OUTRO",
                message = "Tipo deve ser: CAPACITACAO, DOACAO, ATENDIMENTO, EMPREGO ou OUTRO")
        String tipoImpacto,

        @Size(max = 500) String descricao
) {}
