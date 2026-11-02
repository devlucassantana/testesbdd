package br.com.openinvestment.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AlocacaoRequest(
        @NotNull(message = "ID do projeto é obrigatório")
        Long idProjeto,

        @Size(max = 300) String descricao,

        @NotNull @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotBlank(message = "Tipo de recurso é obrigatório")
        @Pattern(regexp = "FINANCEIRO|MATERIAL|HUMANO|TECNOLOGICO",
                message = "Tipo deve ser: FINANCEIRO, MATERIAL, HUMANO ou TECNOLOGICO")
        String tipoRecurso,

        LocalDate dataAlocacao
) {}
