package br.com.openinvestment.dto.request;

import jakarta.validation.constraints.*;

public record BeneficiarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 14) String cpf,
        @Size(max = 50) String perfil,
        @Size(max = 100) String regiao
) {}
