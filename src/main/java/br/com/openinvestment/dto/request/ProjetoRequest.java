package br.com.openinvestment.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoRequest(
        @NotBlank(message = "Nome do projeto é obrigatório")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
        String nomeProjeto,

        String descricao,

        @Size(max = 100, message = "ODS deve ter no máximo 100 caracteres")
        String odsVinculacao,

        @NotNull(message = "Data de início é obrigatória")
        LocalDate dataInicio,

        LocalDate dataFimPrev,

        @NotNull(message = "Orçamento total é obrigatório")
        @DecimalMin(value = "0.01", message = "Orçamento deve ser maior que zero")
        BigDecimal orcamentoTotal
) {}
