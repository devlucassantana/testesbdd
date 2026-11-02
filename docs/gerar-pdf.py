"""Gera documentacao-devops.pdf. Uso (na raiz do projeto): pip install reportlab && python docs/gerar-pdf.py
Prints: coloque os PNGs em docs/prints/ com os nomes da lista PRINTS; os que faltarem viram quadros de aviso."""
import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.lib.utils import ImageReader
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle,
                                Preformatted, Image, KeepTogether)

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PR = os.path.join(ROOT, "docs", "prints")
PRINTS = [
    ("01-pipeline-build-test.png", "Pipeline: jobs Build & Test e Docker Build"),
    ("02-deploy-staging.png", "Pipeline: deploy em Staging (resumo do job com smoke test)"),
    ("03-deploy-production.png", "Pipeline: deploy em Producao (resumo do job com smoke test)"),
    ("04-staging-funcionando.png", "Staging funcionando (porta 8081): health e login"),
    ("05-producao-funcionando.png", "Producao funcionando (porta 8080): health e login"),
    ("06-swagger-ui.png", "Swagger UI com os endpoints"),
]
have = lambda n: os.path.exists(os.path.join(PR, n))
deploy_ok = have("02-deploy-staging.png") and have("03-deploy-production.png")
prints_ok = all(have(n) for n, _ in PRINTS[:5])

ss = getSampleStyleSheet()
B = ParagraphStyle("B", parent=ss["Normal"], fontSize=10, leading=14)
H1 = ParagraphStyle("H1", parent=ss["Heading1"], fontSize=16, textColor=colors.HexColor("#1a3c6e"), spaceBefore=10)
H2 = ParagraphStyle("H2", parent=ss["Heading2"], fontSize=12, textColor=colors.HexColor("#1a3c6e"))
CODE = ParagraphStyle("C", fontName="Courier", fontSize=7.5, leading=9.5)
def P(t, s=B): return Paragraph(t, s)
def code(t): return Preformatted(t, CODE, maxLineLength=105)
def table(rows, widths, head=True):
    t = Table([[P(str(c)) for c in r] for r in rows], colWidths=widths, repeatRows=1 if head else 0)
    st = [("GRID", (0,0), (-1,-1), .4, colors.grey), ("VALIGN", (0,0), (-1,-1), "TOP")]
    if head: st.append(("BACKGROUND", (0,0), (-1,0), colors.HexColor("#dde6f3")))
    t.setStyle(TableStyle(st)); return t
def shot(name, caption):
    path = os.path.join(PR, name)
    items = [P("<b>%s</b>" % caption)]
    if os.path.exists(path):
        w, h = ImageReader(path).getSize(); mw = 16.5*cm; mh = 11*cm
        sc = min(mw/w, mh/h); items.append(Image(path, w*sc, h*sc))
    else:
        t = Table([[P("PRINT PENDENTE: adicione docs/prints/%s e rode docs/gerar-pdf.py novamente." % name)]], colWidths=[16.5*cm], rowHeights=[2.5*cm])
        t.setStyle(TableStyle([("BOX", (0,0), (-1,-1), 1, colors.red), ("VALIGN", (0,0), (-1,-1), "MIDDLE")]))
        items.append(t)
    items.append(Spacer(1, 10)); return KeepTogether(items)

dockerfile = open(os.path.join(ROOT, "Dockerfile"), encoding="utf-8").read()
S = []
S += [Spacer(1, 4*cm), P("Open Investment", ParagraphStyle("T", parent=ss["Title"], fontSize=28)),
      P("Projeto Cidades ESG Inteligentes - Gerenciamento de Projetos Sociais", ParagraphStyle("S", parent=B, fontSize=13, alignment=1)),
      Spacer(1, 1*cm), P("Documentação técnica - Práticas de DevOps", ParagraphStyle("S2", parent=B, fontSize=12, alignment=1)),
      Spacer(1, 2*cm), P("FIAP | Análise e Desenvolvimento de Sistemas | Turma 2TDSOC", ParagraphStyle("S3", parent=B, alignment=1)),
      P("Integrante: Lucas Gabriel Neres Sant'Ana - RM 565612", ParagraphStyle("S4", parent=B, alignment=1)), PageBreak()]

S += [P("1. O projeto", H1),
      P("API REST em Java 17 / Spring Boot 3.2 com Oracle XE 21c para gestão de projetos sociais no contexto ESG (pilares Social e Governance): "
        "controle financeiro com bloqueio de orçamento e alertas, impacto social, voluntários, beneficiários e trilha de auditoria. "
        "32 endpoints em 8 controllers, autenticação JWT com perfis ADMIN/GESTOR/USER, Flyway para o schema e Swagger UI."),
      Spacer(1, 6),
      table([["Componente", "Tecnologia"], ["Linguagem / Framework", "Java 17, Spring Boot 3.2.5"],
             ["Segurança", "Spring Security 6 + JWT (JJWT 0.12)"], ["Banco", "Oracle XE 21c (container) + Flyway"],
             ["Testes", "JUnit 5, Mockito, AssertJ, H2 em memória"], ["Build", "Maven 3.9"],
             ["Containerização", "Docker (multi-stage) + Docker Compose"], ["CI/CD", "GitHub Actions"]], [5*cm, 11.5*cm])]

S += [P("2. Pipeline CI/CD (GitHub Actions)", H1),
      P("Arquivo: <b>.github/workflows/ci.yml</b>. Dispara em push e pull request nas branches main e develop. "
        "Os jobs de deploy só rodam em push: <b>develop = Staging</b> e <b>main = Produção</b>, cada um vinculado a um GitHub Environment."),
      Spacer(1, 6),
      table([["Job", "Quando", "O que faz"],
             ["build-and-test", "Todo push/PR", "Java 17, mvn package -DskipTests (build), mvn test (testes), publica relatórios surefire e o JAR"],
             ["docker-build", "Após testes OK", "docker build da imagem (Dockerfile multi-stage) e listagem da imagem"],
             ["deploy-staging", "Push em develop", "docker compose -p openinvest-stg --env-file env/staging.env up -d --build --wait; smoke test na porta 8081; resumo no job; derruba o ambiente"],
             ["deploy-production", "Push em main", "Mesmo fluxo com -p openinvest-prod e env/production.env; smoke test na porta 8080"]],
            [3.2*cm, 3*cm, 10.3*cm]),
      Spacer(1, 6),
      P("<b>Smoke test</b> (scripts/smoke-test.sh): (1) /actuator/health = UP; (2) login com o usuário do seed e recebimento do token JWT; "
        "(3) GET /api/projetos com token = 200; (4) o mesmo GET sem token = 401. Qualquer falha reprova o job."),
      Spacer(1, 4),
      P("<b>Proteções:</b> deploy depende de build, testes e imagem; DB_PASS e JWT_SECRET vêm de GitHub Secrets quando configurados "
        "(senão usam os valores de demonstração de env/*.env); staging e produção usam projetos Compose, redes, volumes e portas separados."),
      Spacer(1, 4),
      P("<b>Limitação assumida:</b> o deploy é efêmero. A stack real (Oracle + API) sobe no runner do GitHub, é validada e derrubada ao fim do job; "
        "não há servidor externo permanente. A evidência é o resumo do job e os logs do Actions.")]

S += [PageBreak(), P("3. Docker", H1),
      P("<b>Arquitetura:</b> dois serviços (oracle-db e api) na rede app-net; volume oracle-data persiste o banco; configuração por variáveis de ambiente "
        "(API_PORT, DB_USER, DB_PASS, JWT_SECRET, SPRING_PROFILES_ACTIVE); a API só sobe quando o Oracle está healthy (depends_on com service_healthy). "
        "O mesmo docker-compose.yml atende os dois ambientes; muda apenas o arquivo env/*.env e o nome do projeto (-p)."),
      Spacer(1, 6),
      table([["", "Staging", "Produção"], ["Projeto Compose", "openinvest-stg", "openinvest-prod"], ["Porta da API", "8081", "8080"],
             ["Usuário do banco", "openinvest_stg", "openinvest"], ["Arquivo de variáveis", "env/staging.env", "env/production.env"]],
            [4.5*cm, 6*cm, 6*cm]),
      Spacer(1, 6), P("<b>Comandos usados:</b>"),
      code("docker build -t open-investment-api .\n"
           "docker compose -p openinvest-stg  --env-file env/staging.env    up -d --build --wait\n"
           "docker compose -p openinvest-prod --env-file env/production.env up -d --build --wait\n"
           "bash scripts/smoke-test.sh 8081   # staging\n"
           "bash scripts/smoke-test.sh 8080   # producao\n"
           "docker compose -p openinvest-stg --env-file env/staging.env down -v"),
      Spacer(1, 6), P("<b>Imagem criada:</b> open-investment-api (multi-stage: Maven + JDK 17 no build; eclipse-temurin:17-jre-alpine no runtime, usuário sem root, HEALTHCHECK)."),
      Spacer(1, 6), P("<b>Dockerfile:</b>"), code(dockerfile)]

S += [PageBreak(), P("4. Prints do pipeline", H1)] + [shot(*PRINTS[i]) for i in (0, 1, 2)]
S += [PageBreak(), P("5. Staging e Produção funcionando", H1)] + [shot(*PRINTS[i]) for i in (3, 4, 5)]

S += [PageBreak(), P("6. Desafios encontrados e como foram resolvidos", H1)]
ch = [("Dependência circular no Spring Security", "SecurityConfig, JwtAuthFilter e UserDetailsService formavam um ciclo.", "UserDetailsServiceImpl virou @Service independente."),
      ("Testes sem Oracle no CI", "O Oracle não está disponível nos runners para testes unitários.", "Perfil test com H2 em memória e Flyway desabilitado; Mockito nos serviços."),
      ("Hashes bcrypt do seed", "Os hashes não correspondiam às senhas documentadas.", "Hashes regerados e verificados com checkpw antes de entrar no SQL."),
      ("Loop infinito na serialização JSON", "Relações @ManyToOne causavam StackOverflowError.", "@JsonIgnoreProperties nos campos de relação."),
      ("JWT malformado retornando 500", "JwtException no filtro não era tratada.", "try/catch no filtro; a cadeia segue sem autenticar e o entry point devolve 401 JSON."),
      ("Deploy apenas simulado", "Os jobs de deploy só faziam echo de 'UP'.", "Deploy real efêmero com Docker Compose no runner + smoke test automatizado."),
      ("Staging e produção colidindo", "Mesmo projeto Compose, container_name fixo e portas repetidas faziam o staging recriar a produção.", "Um compose parametrizado, projetos (-p), redes e volumes por ambiente, env/*.env."),
      ("Testes frágeis encontrados na revisão", "Um stub nunca usado reprovaria o teste (strict stubs do Mockito) e o teste web mockava o filtro JWT e o ObjectMapper.", "Stub removido e AuthControllerTest reescrito com filtros desligados e ObjectMapper real.")]
S.append(table([["Desafio", "Problema", "Solução"]] + [list(c) for c in ch], [4.2*cm, 6.2*cm, 6.1*cm]))

S += [Spacer(1, 12), P("7. Checklist de entrega", H1)]
mk = lambda ok: "[x]" if ok else "[ ]"
S.append(table([["Item", "OK"],
    ["Projeto compactado em .ZIP com estrutura organizada", mk(True)], ["Dockerfile funcional", mk(True)],
    ["docker-compose.yml (ou Kubernetes)", mk(True)], ["Pipeline com etapas de build, teste e deploy", mk(True)],
    ["README.md com instruções e prints", mk(prints_ok)], ["Documentação técnica com evidências (PDF)", mk(prints_ok)],
    ["Deploy realizado nos ambientes staging e produção", mk(deploy_ok)]], [14*cm, 2.5*cm]))
if not prints_ok:
    S.append(P("<i>Itens com [ ] dependem dos prints do primeiro run do pipeline (docs/prints/). Rode docs/gerar-pdf.py depois de adicioná-los.</i>"))

out = os.path.join(ROOT, "documentacao-devops.pdf")
SimpleDocTemplate(out, pagesize=A4, leftMargin=2*cm, rightMargin=2*cm, topMargin=2*cm, bottomMargin=2*cm,
                  title="Open Investment - Documentação DevOps", author="Lucas Gabriel Neres Sant'Ana").build(S)
print("PDF gerado:", out)
