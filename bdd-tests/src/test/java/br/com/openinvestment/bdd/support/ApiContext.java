package br.com.openinvestment.bdd.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Estado compartilhado de um cenário (injetado pelo PicoContainer):
 * token, última resposta HTTP, variáveis guardadas e validação de contrato.
 */
public class ApiContext {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final Map<String, JsonSchema> SCHEMAS = new ConcurrentHashMap<>();
    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis());
    private static final Map<String, String[]> CREDENCIAIS = Map.of(
            "ADMIN", new String[]{"admin@openinvestment.com", "Admin@123"},
            "GESTOR", new String[]{"gestor@openinvestment.com", "Gestor@123"});

    private final Map<String, String> variaveis = new HashMap<>();
    private String token;
    private Response response;
    private JsonNode corpo;

    public ApiContext() {
        String unique = Long.toString(SEQUENCE.incrementAndGet());
        variaveis.put("unique", unique);
        variaveis.put("email", "bdd" + unique + "@teste.com");
        long n = ThreadLocalRandom.current().nextLong(10_000_000_000L, 99_999_999_999L);
        String d = Long.toString(n);
        variaveis.put("cpf", d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9, 11));
    }

    // ---------- configuração ----------

    public static String baseUrl() {
        String url = System.getProperty("baseUrl");
        if (url == null || url.isBlank()) {
            url = System.getenv("BASE_URL");
        }
        if (url == null || url.isBlank()) {
            url = "http://localhost:8080";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static RequestSpecification baseSpec() {
        return RestAssured.given()
                .baseUri(baseUrl())
                .contentType("application/json; charset=UTF-8")
                .accept("application/json");
    }

    // ---------- autenticação ----------

    public void authenticateAs(String perfil) {
        this.token = tokenDe(perfil);
    }

    public void clearToken() {
        this.token = null;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String tokenDe(String perfil) {
        return TOKENS.computeIfAbsent(perfil, this::login);
    }

    private String login(String perfil) {
        String[] c = CREDENCIAIS.get(perfil);
        if (c == null) {
            throw new IllegalArgumentException("Perfil desconhecido: " + perfil + " (use ADMIN ou GESTOR)");
        }
        Response r = baseSpec()
                .body("{\"email\":\"" + c[0] + "\",\"senha\":\"" + c[1] + "\"}")
                .post("/api/auth/login");
        if (r.getStatusCode() != 200) {
            throw new AssertionError("Falha no login de " + perfil + ": HTTP " + r.getStatusCode() + " " + r.asString());
        }
        try {
            return MAPPER.readTree(r.asByteArray()).get("token").asText();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- chamadas HTTP ----------

    /** Chamada do cenário: usa o token atual (ou nenhum) e guarda a resposta para as verificações. */
    public void send(String method, String path, String jsonBody) {
        RequestSpecification spec = baseSpec();
        if (token != null) {
            spec.header("Authorization", "Bearer " + token);
        }
        if (jsonBody != null) {
            spec.body(resolve(jsonBody).getBytes(StandardCharsets.UTF_8));
        }
        String alvo = resolve(path);
        this.response = spec.request(method, alvo);
        this.corpo = null;
        System.out.printf("    [HTTP] %s %s -> %d%n", method, alvo, response.getStatusCode());
    }

    /** Pré-condição de cenário: usa sempre o ADMIN e não altera token nem resposta do cenário. */
    public JsonNode setup(String method, String path, String jsonBody, int statusEsperado) {
        RequestSpecification spec = baseSpec().header("Authorization", "Bearer " + tokenDe("ADMIN"));
        if (jsonBody != null) {
            spec.body(resolve(jsonBody).getBytes(StandardCharsets.UTF_8));
        }
        Response r = spec.request(method, resolve(path));
        if (r.getStatusCode() != statusEsperado) {
            throw new AssertionError("Pré-condição falhou: " + method + " " + path
                    + " retornou HTTP " + r.getStatusCode() + " " + r.asString());
        }
        try {
            return MAPPER.readTree(r.asByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- resposta ----------

    public int statusCode() {
        return response.getStatusCode();
    }

    public String rawBody() {
        return response.asString();
    }

    public JsonNode json() {
        if (corpo == null) {
            try {
                corpo = MAPPER.readTree(response.asByteArray());
            } catch (IOException e) {
                throw new AssertionError("O corpo da resposta não é JSON válido: " + response.asString(), e);
            }
        }
        return corpo;
    }

    /** Navega pelo JSON com caminho separado por pontos. Ex.: "campos.nome", "content.0.idLog". */
    public JsonNode at(String caminho) {
        JsonNode n = json();
        for (String parte : caminho.split("\\.")) {
            if (n == null || n.isMissingNode() || n.isNull()) {
                return MissingNode.getInstance();
            }
            n = n.isArray() ? n.path(Integer.parseInt(parte)) : n.path(parte);
        }
        return n;
    }

    // ---------- contrato (JSON Schema) ----------

    public void assertSchema(JsonNode node, String nome) {
        Set<ValidationMessage> erros = schema(nome).validate(node);
        if (!erros.isEmpty()) {
            throw new AssertionError("Contrato '" + nome + "' violado: " + erros + " | corpo: " + node);
        }
    }

    private JsonSchema schema(String nome) {
        return SCHEMAS.computeIfAbsent(nome, n -> {
            try (InputStream in = ApiContext.class.getResourceAsStream("/schemas/" + n + ".schema.json")) {
                if (in == null) {
                    throw new IllegalArgumentException("Schema não encontrado: /schemas/" + n + ".schema.json");
                }
                return SCHEMA_FACTORY.getSchema(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    // ---------- variáveis e placeholders ----------

    public void set(String nome, String valor) {
        variaveis.put(nome, valor);
    }

    /** Troca {{variavel}} pelo valor guardado (unique, cpf, email, projetoId, ...). */
    public String resolve(String texto) {
        String saida = texto;
        for (Map.Entry<String, String> e : variaveis.entrySet()) {
            saida = saida.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return saida;
    }
}
