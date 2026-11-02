package br.com.openinvestment.dto.request;

import jakarta.validation.constraints.*;

public record VoluntarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 100) String especialidade
) {}
