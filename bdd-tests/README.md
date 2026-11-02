# Testes BDD de API - Open Investment

Testes automatizados em **BDD (Gherkin em português + Cucumber)** para a API do Open Investment. Validam **status code**, **corpo JSON** e **contrato (JSON Schema)** dos 32 endpoints, com cenários positivos (caminho feliz) e negativos (falhas).

Alinhamento ESG: **Social** (beneficiários, voluntários, impacto), **Governança/Compliance** (bloqueio de orçamento, alertas, perfis de acesso, trilha de auditoria).

## Estrutura

```
bdd-tests/
├── pom.xml                      # Cucumber 7, JUnit Platform, REST Assured, JSON Schema
├── Dockerfile                   # roda os testes em container
├── src/test/java/.../bdd/
│   ├── RunCucumberTest.java     # ponto de entrada
│   ├── support/ApiContext.java  # HTTP, token, variáveis, validação de contrato
│   └── steps/                   # CommonSteps (genéricos) e SetupSteps (pré-condições)
├── src/test/resources/
│   ├── features/*.feature       # 9 features, 66 cenários (Gherkin)
│   ├── schemas/*.schema.json    # contratos (JSON Schema draft-07)
│   └── junit-platform.properties# relatórios (HTML, JSON, XML)
└── docs/gerar-pdf.py            # gera documentacao-testes-bdd.pdf
```

## Como executar

Os testes chamam uma API **em execução** (padrão `http://localhost:8080`) e criam seus próprios dados, sem depender de IDs fixos.

**1. Suba a API** (na raiz do projeto, com Docker Desktop aberto):
```bash
docker compose up -d --build --wait
```

**2. Rode os testes** (Java 17 e Maven instalados), em outro terminal:
```bash
cd bdd-tests
mvn test
```

Outra URL: `mvn test -DbaseUrl=http://localhost:8081` (ou variável `BASE_URL`).
Só um grupo: `mvn test -Dcucumber.filter.tags="@negativo"` (tags: `@positivo`, `@negativo`, `@contrato`, `@governanca`, `@compliance`).

**Sem Maven local (Docker):**
```bash
docker build -t open-investment-bdd bdd-tests
docker run --rm -e BASE_URL=http://host.docker.internal:8080 open-investment-bdd
```

**No pipeline:** o job `deploy-staging` (`.github/workflows/ci.yml`) sobe o ambiente, roda o smoke test e depois estes testes contra `http://localhost:8081`. Resultado no resumo do job e relatório no artefato `relatorio-bdd-cucumber`.

## Relatórios

Após a execução: `bdd-tests/target/cucumber-report/cucumber.html` (abrir no navegador), `cucumber.json` e `cucumber.xml`. O console mostra cada passo e o total de cenários ao final.

## Credenciais usadas (seed do banco)

`ADMIN`: admin@openinvestment.com / Admin@123 · `GESTOR`: gestor@openinvestment.com / Gestor@123

## Cobertura dos endpoints

| Endpoint | Feature | Cenários |
|---|---|---|
| `POST /api/auth/login` | 01_autenticacao | login ADMIN e GESTOR, senha errada, e-mail inexistente, corpo vazio |
| `GET /api/projetos` | 02_projetos, 09_contratos_api | listar, filtrar por status, contrato |
| `GET /api/projetos/{id}` | 02_projetos | buscar por ID, 404 |
| `GET /api/projetos/orcamento-critico` | 03_alocacoes, 09_contratos_api | projeto com 85% da verba aparece na lista |
| `POST /api/projetos` | 02_projetos | criar (ADMIN e GESTOR), 3 validações (400) |
| `PUT /api/projetos/{id}` | 02_projetos | atualizar |
| `PATCH /api/projetos/{id}/status` | 02_projetos | EM_ANDAMENTO (204), status inválido (422) |
| `DELETE /api/projetos/{id}` | 02_projetos | ADMIN exclui (204), GESTOR bloqueado (403) |
| `GET /api/alocacoes/projeto/{id}` | 03_alocacoes | listar por projeto |
| `GET /api/alocacoes/{id}` | 03_alocacoes | buscar, 404 |
| `POST /api/alocacoes` | 03_alocacoes | dentro do orçamento (201), acima (422), 2 validações (400), projeto inexistente (404) |
| `DELETE /api/alocacoes/{id}` | 03_alocacoes | excluir devolve saldo |
| `GET /api/impactos/projeto/{id}` | 04_impactos | listar, projeto inexistente (404) |
| `GET /api/impactos/projeto/{id}/contagem` | 04_impactos | contagem |
| `POST /api/impactos` | 04_impactos | registrar (inicia o projeto), tipo inválido, beneficiário e projeto inexistentes |
| `GET /api/beneficiarios` | 05_beneficiarios, 09_contratos_api | listar |
| `GET /api/beneficiarios/{id}` | 05_beneficiarios | buscar, 404 |
| `POST /api/beneficiarios` | 05_beneficiarios | criar, CPF duplicado (422), nome em branco (400) |
| `PUT /api/beneficiarios/{id}` | 05_beneficiarios | atualizar |
| `DELETE /api/beneficiarios/{id}` | 05_beneficiarios | excluir (204) e confirmar 404 |
| `GET /api/voluntarios` | 06_voluntarios, 09_contratos_api | listar |
| `GET /api/voluntarios/{id}` | 06_voluntarios | buscar, 404 |
| `POST /api/voluntarios` | 06_voluntarios | criar, CPF duplicado (422), e-mail inválido (400) |
| `PUT /api/voluntarios/{id}` | 06_voluntarios | atualizar |
| `PATCH /api/voluntarios/{id}/inativar` | 06_voluntarios | inativar |
| `POST /api/voluntarios/{id}/vincular-projeto/{idProjeto}` | 06_voluntarios, 08_auditoria_logs | vincular gera alerta e log; projeto inexistente (404) |
| `GET /api/alertas/pendentes` | 07_alertas, 09_contratos_api | listar pendentes, sem token (401) |
| `GET /api/alertas/projeto/{id}` | 07_alertas, 03_alocacoes | listar por projeto |
| `PATCH /api/alertas/{id}/ler` | 07_alertas | marcar como lido, 404 |
| `PATCH /api/alertas/{id}/resolver` | 07_alertas | resolver, 404 |
| `GET /api/logs` | 08_auditoria_logs | ADMIN (200), GESTOR (403) |
| `GET /api/logs/tabela/{tabela}` | 08_auditoria_logs | rastreabilidade de INSERT, sem token (401) |

## Defeito encontrado pelos testes

Ao escrever o cenário "login com senha incorreta", a análise mostrou que a API respondia **500** (a exceção de autenticação caía no handler genérico). Foi corrigido em `GlobalExceptionHandler` (`AuthenticationException` agora retorna **401** com o corpo padrão de erro) e o cenário passou a validar esse comportamento.

## Como adicionar um cenário

Escreva em um `.feature` usando os passos existentes (ex.: `Quando envio "GET" para "/api/projetos"`, `Então o status da resposta é 200`, `E o corpo respeita o contrato "projeto"`). Placeholders: `{{unique}}`, `{{cpf}}`, `{{email}}` e os IDs guardados (`{{projetoId}}`, `{{beneficiarioId}}`, `{{voluntarioId}}`, `{{alocacaoId}}`, `{{impactoId}}`).
