# Projeto - Cidades ESG Inteligentes (Open Investment)

**FIAP · ADS · Turma 2TDSOC** · Lucas Gabriel Neres Sant'Ana · RM 565612

API REST (Java 17 / Spring Boot 3.2 / Oracle XE 21c) para gestão de projetos sociais ESG: controle financeiro com bloqueio de orçamento e alertas, impacto social, voluntários, beneficiários e auditoria. 32 endpoints, JWT com perfis ADMIN/GESTOR/USER, Flyway e Swagger UI. Documentação técnica completa: `documentacao-devops.pdf`.

## Como executar localmente com Docker

Pré-requisitos: Docker Desktop e ~4 GB de RAM livres (Oracle XE).

```bash
cp .env.example .env
docker compose up --build        # a 1ª subida leva ~2-3 min (Oracle)
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Login de teste: `admin@openinvestment.com` / `Admin@123` (ADMIN) · `gestor@openinvestment.com` / `Gestor@123` (GESTOR)

Ambientes separados (mesmo compose, projetos/portas/volumes/redes distintos):

```bash
docker compose -p openinvest-stg  --env-file env/staging.env    up -d --build --wait   # http://localhost:8081
docker compose -p openinvest-prod --env-file env/production.env up -d --build --wait   # http://localhost:8080
bash scripts/smoke-test.sh 8081      # valida health, login, endpoint protegido (200) e sem token (401)
docker compose -p openinvest-stg --env-file env/staging.env down -v   # parar e apagar dados
```

Testes (sem Oracle, usam H2 em memória): `mvn test -Dspring.profiles.active=test`

## Pipeline CI/CD

Ferramenta: **GitHub Actions** (`.github/workflows/ci.yml`). Push/PR em `main` e `develop`.

| Job | Quando | O que faz |
|---|---|---|
| `build-and-test` | todo push/PR | Java 17, `mvn package -DskipTests` (build), `mvn test` (testes), publica relatórios e JAR |
| `docker-build` | após testes OK | `docker build` da imagem |
| `deploy-staging` | push em `develop` | Compose (`-p openinvest-stg`), smoke test na porta 8081, resumo no job |
| `deploy-production` | push em `main` | Compose (`-p openinvest-prod`), smoke test na porta 8080, resumo no job |

O deploy é **real porém efêmero**: Oracle + API sobem no runner do GitHub, o smoke test valida o ambiente e o resultado fica no resumo do job; ao final a stack é derrubada (não há servidor permanente).

Como ativar: crie o repositório, faça push de `main` e `develop` (o deploy de staging roda em `develop` e o de produção em `main`). Os Environments `staging` e `production` são criados automaticamente no primeiro run; opcionalmente configure os Secrets `JWT_SECRET` e `DB_PASS` (sem eles valem os valores de demonstração de `env/*.env`).

## Containerização

Estratégias: build multi-stage (Maven + JDK no build, JRE Alpine no runtime), cache de dependências em camada própria, usuário sem root, HEALTHCHECK, Oracle com volume `oracle-data`, rede `app-net`, variáveis por ambiente e `depends_on` com `service_healthy`.

```dockerfile
# =============================================
# Dockerfile — Open Investment API
# Multi-stage build: compila com Maven e roda com JRE slim
# =============================================

# ---- Estágio 1: Build ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila (sem rodar testes — Oracle não estará disponível)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Estágio 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário sem privilégios de root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia o JAR gerado no estágio de build
COPY --from=builder /app/target/open-investment-1.0.0.jar app.jar

# Porta padrão da aplicação
EXPOSE 8080

# Variáveis não sensíveis com padrão; DB_USER, DB_PASS e JWT_SECRET vêm do compose/.env
ENV DB_HOST=oracle-db \
    DB_PORT=1521 \
    DB_SERVICE=XEPDB1

# Health check para o Docker aguardar a app estar pronta
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Prints do funcionamento

Capturas do primeiro run do pipeline (aba Actions > job > resumo) e dos ambientes. Salve em `docs/prints/` com estes nomes:

![Build e testes](docs/prints/01-pipeline-build-test.png)
![Deploy staging](docs/prints/02-deploy-staging.png)
![Deploy produção](docs/prints/03-deploy-production.png)
![Staging funcionando](docs/prints/04-staging-funcionando.png)
![Produção funcionando](docs/prints/05-producao-funcionando.png)
![Swagger UI](docs/prints/06-swagger-ui.png)

Depois de adicionar os prints: `pip install reportlab && python docs/gerar-pdf.py` atualiza o PDF.

## Testes automatizados BDD (Cucumber)

Projeto de testes em `bdd-tests/` (Gherkin em português + REST Assured + JSON Schema) cobrindo os 32 endpoints, com cenários positivos, negativos e de contrato. Roda no pipeline (job `deploy-staging`, após o smoke test) e também localmente. Passo a passo, mapa de cobertura e relatórios: [`bdd-tests/README.md`](bdd-tests/README.md).

```bash
docker compose up -d --build --wait          # API em http://localhost:8080
mvn -f bdd-tests/pom.xml test                # requer Java 17 e Maven
```

## Tecnologias utilizadas

Java 17 · Spring Boot 3.2 · Spring Security 6 + JWT (JJWT 0.12) · Spring Data JPA/Hibernate 6 · Oracle XE 21c · Flyway · Maven 3.9 · JUnit 5 + Mockito + AssertJ + H2 · SpringDoc OpenAPI · Docker + Docker Compose · GitHub Actions.

## Checklist de entrega

| Item | OK |
|---|---|
| Projeto compactado em .ZIP com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| docker-compose.yml | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| README.md com instruções e prints | ☐ (falta adicionar os prints) |
| Documentação técnica com evidências (PDF) | ☐ (regerar após os prints) |
| Deploy realizado nos ambientes staging e produção | ☐ (marcar após o 1º run verde) |
