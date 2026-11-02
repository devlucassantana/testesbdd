package br.com.openinvestment.bdd.steps;

import br.com.openinvestment.bdd.support.ApiContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;

/** Pré-condições: criam dados via API (como ADMIN) e guardam os IDs em variáveis do cenário. */
public class SetupSteps {

    private final ApiContext ctx;

    public SetupSteps(ApiContext ctx) {
        this.ctx = ctx;
    }

    @Given("que existe um projeto com orçamento de {int} reais")
    public void projeto(int orcamento) {
        JsonNode r = ctx.setup("POST", "/api/projetos",
                "{\"nomeProjeto\":\"Projeto BDD {{unique}}\",\"descricao\":\"Criado pelo teste automatizado\","
                        + "\"odsVinculacao\":\"ODS 4\",\"dataInicio\":\"2026-01-01\",\"dataFimPrev\":\"2026-12-31\","
                        + "\"orcamentoTotal\":" + orcamento + ".00}", 201);
        ctx.set("projetoId", r.get("idProjeto").asText());
    }

    @Given("que existe um beneficiário cadastrado")
    public void beneficiario() {
        JsonNode r = ctx.setup("POST", "/api/beneficiarios",
                "{\"nome\":\"Beneficiario BDD {{unique}}\",\"cpf\":\"{{cpf}}\","
                        + "\"perfil\":\"Jovem 18-25\",\"regiao\":\"Zona Leste - SP\"}", 201);
        ctx.set("beneficiarioId", r.get("idBeneficiario").asText());
    }

    @Given("que existe um voluntário cadastrado")
    public void voluntario() {
        JsonNode r = ctx.setup("POST", "/api/voluntarios",
                "{\"nome\":\"Voluntario BDD {{unique}}\",\"cpf\":\"{{cpf}}\",\"email\":\"{{email}}\","
                        + "\"especialidade\":\"Tecnologia da Informacao\"}", 201);
        ctx.set("voluntarioId", r.get("idVoluntario").asText());
    }

    @Given("que existe uma alocação de {int} reais neste projeto")
    public void alocacao(int valor) {
        JsonNode r = ctx.setup("POST", "/api/alocacoes",
                "{\"idProjeto\":{{projetoId}},\"descricao\":\"Alocacao BDD\",\"valor\":" + valor
                        + ".00,\"tipoRecurso\":\"FINANCEIRO\"}", 201);
        ctx.set("alocacaoId", r.get("idAlocacao").asText());
    }

    @Given("que existe um impacto registrado neste projeto")
    public void impacto() {
        JsonNode r = ctx.setup("POST", "/api/impactos",
                "{\"idProjeto\":{{projetoId}},\"idBeneficiario\":{{beneficiarioId}},"
                        + "\"tipoImpacto\":\"CAPACITACAO\",\"descricao\":\"Impacto BDD\"}", 201);
        ctx.set("impactoId", r.get("idImpacto").asText());
    }
}
