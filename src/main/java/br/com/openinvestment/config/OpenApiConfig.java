package br.com.openinvestment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "Open Investment — API de Projetos Sociais ESG",
        version     = "1.0",
        description = "API RESTful para gerenciamento de projetos sociais com governança ESG. " +
                      "Controla orçamento, voluntários, beneficiários e impactos sociais. " +
                      "Autenticação via JWT (Bearer Token). " +
                      "FIAP 2TDSOC · Lucas Gabriel Neres Sant'Ana · RM 565612",
        contact     = @Contact(name = "Lucas Gabriel", email = "lucas@openinvestment.com")
    )
)
@SecurityScheme(
    name   = "bearerAuth",
    type   = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {}
