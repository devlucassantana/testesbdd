package br.com.openinvestment.bdd.steps;

import br.com.openinvestment.bdd.support.ApiContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final ApiContext ctx;

    public CommonSteps(ApiContext ctx) {
        this.ctx = ctx;
    }

    private static String texto(JsonNode n) {
        return (n == null || n.isMissingNode() || n.isNull()) ? null : n.asText();
    }

    // ---------- autenticação ----------

    @Given("que estou autenticado como {string}")
    public void autenticadoComo(String perfil) {
        ctx.authenticateAs(perfil);
    }

    @Given("que não estou autenticado")
    public void naoAutenticado() {
        ctx.clearToken();
    }

    @Given("que uso o token inválido {string}")
    public void tokenInvalido(String token) {
        ctx.setToken(token);
    }

    // ---------- chamadas ----------

    @When("envio {string} para {string}")
    public void enviar(String metodo, String caminho) {
        ctx.send(metodo, caminho, null);
    }

    @When("envio {string} para {string} com o corpo:")
    public void enviarComCorpo(String metodo, String caminho, String corpo) {
        ctx.send(metodo, caminho, corpo);
    }

    @When("guardo o campo {string} da resposta como {string}")
    public void guardarCampo(String campo, String variavel) {
        String valor = texto(ctx.at(campo));
        assertThat(valor).as("campo '%s' presente na resposta: %s", campo, ctx.rawBody()).isNotNull();
        ctx.set(variavel, valor);
    }

    // ---------- verificações ----------

    @Then("o status da resposta é {int}")
    public void status(int esperado) {
        assertThat(ctx.statusCode()).as("status HTTP (corpo: %s)", ctx.rawBody()).isEqualTo(esperado);
    }

    @Then("o corpo respeita o contrato {string}")
    public void contrato(String schema) {
        ctx.assertSchema(ctx.json(), schema);
    }

    @Then("o corpo é uma lista")
    public void corpoEhLista() {
        assertThat(ctx.json().isArray()).as("o corpo deveria ser uma lista JSON: %s", ctx.rawBody()).isTrue();
    }

    @Then("cada item da lista respeita o contrato {string}")
    public void contratoDaLista(String schema) {
        JsonNode lista = ctx.json();
        assertThat(lista.isArray()).as("o corpo deveria ser uma lista JSON").isTrue();
        for (JsonNode item : lista) {
            ctx.assertSchema(item, schema);
        }
    }

    @Then("o campo {string} é {string}")
    public void campoIgual(String campo, String esperado) {
        assertThat(texto(ctx.at(campo))).as("campo '%s'", campo).isEqualTo(ctx.resolve(esperado));
    }

    @Then("o campo {string} contém {string}")
    public void campoContem(String campo, String trecho) {
        assertThat(texto(ctx.at(campo))).as("campo '%s'", campo).contains(ctx.resolve(trecho));
    }

    @Then("o campo numérico {string} é igual a {string}")
    public void campoNumerico(String campo, String esperado) {
        String valor = texto(ctx.at(campo));
        assertThat(valor).as("campo numérico '%s' presente: %s", campo, ctx.rawBody()).isNotNull();
        assertThat(new BigDecimal(valor).compareTo(new BigDecimal(ctx.resolve(esperado))))
                .as("campo '%s' = %s, esperado %s", campo, valor, esperado).isZero();
    }

    @Then("o campo {string} existe")
    public void campoExiste(String campo) {
        assertThat(texto(ctx.at(campo))).as("campo '%s' deveria existir: %s", campo, ctx.rawBody()).isNotNull();
    }

    @Then("a lista contém um item com {string} igual a {string}")
    public void listaContem(String campo, String esperado) {
        verificarLista(ctx.json(), campo, esperado);
    }

    @Then("a lista em {string} contém um item com {string} igual a {string}")
    public void listaEmContem(String caminho, String campo, String esperado) {
        verificarLista(ctx.at(caminho), campo, esperado);
    }

    private void verificarLista(JsonNode lista, String campo, String esperado) {
        assertThat(lista.isArray()).as("o valor deveria ser uma lista JSON").isTrue();
        String alvo = ctx.resolve(esperado);
        boolean achou = false;
        for (JsonNode item : lista) {
            if (alvo.equals(texto(item.path(campo)))) {
                achou = true;
                break;
            }
        }
        assertThat(achou).as("lista deveria conter item com %s = %s", campo, alvo).isTrue();
    }
}
