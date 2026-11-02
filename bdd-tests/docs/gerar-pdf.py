"""Gera documentacao-testes-bdd.pdf. Uso (dentro de bdd-tests): pip install reportlab && python docs/gerar-pdf.py
Prints: coloque os PNGs em docs/prints/ com os nomes da lista PRINTS; os que faltarem viram quadros de aviso."""
import os, glob
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.lib.utils import ImageReader
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle,
                                Preformatted, Image, KeepTogether)

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))      # bdd-tests/
PR = os.path.join(ROOT, "docs", "prints")
PRINTS = [
    ("01-execucao-local-console.png", "Execução local: console do mvn test com o resumo de cenários e passos"),
    ("02-relatorio-html-cucumber.png", "Relatório HTML do Cucumber (target/cucumber-report/cucumber.html)"),
    ("03-pipeline-bdd-actions.png", "GitHub Actions: job Deploy - Staging com o passo de testes BDD verde"),
    ("04-resumo-bdd-no-job.png", "GitHub Actions: resumo do job com o total de cenários executados"),
]
have = lambda n: os.path.exists(os.path.join(PR, n))
prints_ok = all(have(n) for n, _ in PRINTS)

ss = getSampleStyleSheet()
B = ParagraphStyle("B", parent=ss["Normal"], fontSize=10, leading=14)
H1 = ParagraphStyle("H1", parent=ss["Heading1"], fontSize=16, textColor=colors.HexColor("#1a3c6e"), spaceBefore=10)
H2 = ParagraphStyle("H2", parent=ss["Heading2"], fontSize=11.5, textColor=colors.HexColor("#1a3c6e"))
CODE = ParagraphStyle("C", fontName="Courier", fontSize=7, leading=8.8)
def esc(t): return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
def P(t, s=B): return Paragraph(t, s)
def code(t): return Preformatted(t, CODE, maxLineLength=110)
def read(*p): return open(os.path.join(*p), encoding="utf-8").read().rstrip()
def table(rows, widths):
    t = Table([[P(esc(str(c))) for c in r] for r in rows], colWidths=widths, repeatRows=1)
    t.setStyle(TableStyle([("GRID", (0,0), (-1,-1), .4, colors.grey), ("VALIGN", (0,0), (-1,-1), "TOP"),
                           ("BACKGROUND", (0,0), (-1,0), colors.HexColor("#dde6f3"))])); return t
def shot(name, caption):
    path = os.path.join(PR, name); items = [P("<b>" + esc(caption) + "</b>")]
    if os.path.exists(path):
        w, h = ImageReader(path).getSize(); sc = min(16.5*cm/w, 11*cm/h); items.append(Image(path, w*sc, h*sc))
    else:
        t = Table([[P("PRINT PENDENTE: adicione docs/prints/" + name + " e rode docs/gerar-pdf.py novamente.")]],
                  colWidths=[16.5*cm], rowHeights=[2.5*cm])
        t.setStyle(TableStyle([("BOX", (0,0), (-1,-1), 1, colors.red), ("VALIGN", (0,0), (-1,-1), "MIDDLE")])); items.append(t)
    items.append(Spacer(1, 10)); return KeepTogether(items)

# tabela de cobertura: lida do README.md (uma única fonte da verdade)
COVERAGE = []
for line in read(ROOT, "README.md").splitlines():
    if line.startswith("| `"):
        c = [x.strip().strip("`") for x in line.strip().strip("|").split("|")]
        COVERAGE.append(c)
assert len(COVERAGE) == 32, "tabela de cobertura do README deve ter 32 linhas"

features = sorted(glob.glob(os.path.join(ROOT, "src", "test", "resources", "features", "*.feature")))
schemas = sorted(glob.glob(os.path.join(ROOT, "src", "test", "resources", "schemas", "*.json")))
S = [Spacer(1, 4*cm), P("Open Investment", ParagraphStyle("T", parent=ss["Title"], fontSize=28)),
     P("Testes automatizados de API com BDD (Gherkin / Cucumber)", ParagraphStyle("S", parent=B, fontSize=13, alignment=1)),
     Spacer(1, 1*cm), P("Projeto Cidades ESG Inteligentes - Gerenciamento de Projetos Sociais", ParagraphStyle("S2", parent=B, fontSize=11, alignment=1)),
     Spacer(1, 2*cm), P("FIAP | Análise e Desenvolvimento de Sistemas | Turma 2TDSOC", ParagraphStyle("S3", parent=B, alignment=1)),
     P("Integrante: Lucas Gabriel Neres Sant'Ana - RM 565612", ParagraphStyle("S4", parent=B, alignment=1)), PageBreak()]

S += [P("1. Objetivo e alinhamento ESG", H1),
      P("Validar de forma automatizada as funcionalidades críticas da API Open Investment (Java 17, Spring Boot 3.2, Oracle XE), usando BDD. "
        "Os testes cobrem os 32 endpoints com validação de status code, corpo JSON e contrato por JSON Schema, em cenários positivos e negativos."),
      Spacer(1, 4),
      table([["Pilar ESG", "O que os testes garantem"],
             ["Social", "Cadastro de beneficiários e voluntários, registro de impacto social, início automático do projeto ao registrar o primeiro impacto, bloqueio de CPF duplicado."],
             ["Governança / Compliance", "Bloqueio de alocação acima do orçamento, alerta ao atingir 80% da verba, controle de acesso por perfil (ADMIN, GESTOR), rejeição de token ausente ou inválido, trilha de auditoria de operações."]],
            [4*cm, 12.5*cm]),
      P("2. Estratégia e ferramentas", H1),
      table([["Item", "Escolha"],
             ["BDD", "Cucumber 7 com Gherkin em português (66 cenários em 9 features, 438 passos)"],
             ["Testes de API", "REST Assured 5 (HTTP) + AssertJ"],
             ["Contrato", "JSON Schema (draft-07) validado com networknt json-schema-validator; %d schemas" % len(schemas)],
             ["Execução", "JUnit Platform Suite + Maven; local, Docker ou GitHub Actions"],
             ["Dados", "Cada cenário cria seus próprios dados via API (sem IDs fixos) e usa o seed apenas para as credenciais"]],
            [3.5*cm, 13*cm]),
      Spacer(1, 6),
      P("Os cenários negativos cobrem: dados inválidos (400), recurso inexistente (404), regra de negócio violada (422), "
        "não autenticado (401) e sem permissão (403). As tags @positivo, @negativo, @contrato, @governanca e @compliance permitem executar grupos específicos.")]

S += [PageBreak(), P("3. Cobertura dos 32 endpoints", H1),
      table([["Endpoint", "Feature", "Cenários"]] + COVERAGE, [5.3*cm, 3.7*cm, 7.5*cm])]

S += [PageBreak(), P("4. Cenários em Gherkin", H1),
      P("Texto integral das features executadas (arquivos em src/test/resources/features).")]
for f in features:
    S += [Spacer(1, 6), P(esc(os.path.basename(f)), H2), code(read(f))]

S += [PageBreak(), P("5. Testes de contrato (JSON Schema)", H1),
      P("Cada resposta de sucesso e de erro é validada contra um schema em src/test/resources/schemas: "
        + ", ".join(esc(os.path.basename(s).replace(".schema.json", "")) for s in schemas)
        + ". Exemplo (projeto.schema.json):"),
      code(read(ROOT, "src", "test", "resources", "schemas", "projeto.schema.json"))]

S += [PageBreak(), P("6. Configuração e execução", H1), P("<b>junit-platform.properties</b>"),
      code(read(ROOT, "src", "test", "resources", "junit-platform.properties")), Spacer(1, 6), P("<b>Dockerfile</b>"),
      code(read(ROOT, "Dockerfile")), Spacer(1, 6), P("<b>Passo do pipeline (.github/workflows/ci.yml, job deploy-staging)</b>"),
      code("- name: Testes BDD de API (Cucumber + REST Assured + JSON Schema)\n  run: mvn -B -ntp -f bdd-tests/pom.xml test | tee bdd.log\n  env:\n    BASE_URL: http://localhost:8081"),
      Spacer(1, 6), P("<b>Execução local</b>"),
      code("docker compose up -d --build --wait      # na raiz do projeto: API em http://localhost:8080\n"
           "cd bdd-tests && mvn test                   # relatorio: target/cucumber-report/cucumber.html\n"
           "mvn test -Dcucumber.filter.tags=\"@negativo\"  # somente cenarios negativos\n"
           "docker build -t open-investment-bdd . && docker run --rm -e BASE_URL=http://host.docker.internal:8080 open-investment-bdd"),
      Spacer(1, 6), P("<b>Dependências principais (pom.xml)</b>"),
      code("io.cucumber:cucumber-java, cucumber-junit-platform-engine, cucumber-picocontainer (7.15.0)\n"
           "org.junit.platform:junit-platform-suite (5.10.2)\nio.rest-assured:rest-assured (5.4.0)\n"
           "com.networknt:json-schema-validator (1.4.0)\ncom.fasterxml.jackson.core:jackson-databind (2.17.0)\norg.assertj:assertj-core (3.25.3)")]

S += [PageBreak(), P("7. Evidências de execução", H1)] + [shot(*p) for p in PRINTS]

S += [PageBreak(), P("8. Defeito encontrado e corrigido", H1),
      P("Ao escrever o cenário negativo de login (senha incorreta), a análise do código mostrou que a API respondia HTTP 500: a exceção de autenticação "
        "era capturada pelo handler genérico de exceções. Correção: novo handler para AuthenticationException em GlobalExceptionHandler, que devolve 401 "
        "com o corpo padrão de erro. O cenário passou a validar o comportamento correto (401 e contrato 'erro').")]
mk = lambda ok: "[x]" if ok else "[ ]"
S += [Spacer(1, 12), P("9. Checklist", H1),
      table([["Item", "OK"], ["Mínimo de 3 cenários BDD em Gherkin (entregues: 66, positivos e negativos)", "[x]"],
             ["Testes de API para todas as APIs (32 endpoints)", "[x]"], ["Validação de status code e corpo JSON", "[x]"],
             ["Testes de contrato com JSON Schema", "[x]"], ["Execução documentada (local, Docker e pipeline)", "[x]"],
             ["Código-fonte, Dockerfile e README no .zip", "[x]"], ["Evidências de execução (prints) neste PDF", mk(prints_ok)]], [14*cm, 2.5*cm])]
if not prints_ok:
    S.append(P("<i>O item de evidências depende dos prints em docs/prints/. Rode docs/gerar-pdf.py depois de adicioná-los.</i>"))

out = os.path.join(ROOT, "documentacao-testes-bdd.pdf")
SimpleDocTemplate(out, pagesize=A4, leftMargin=2*cm, rightMargin=2*cm, topMargin=2*cm, bottomMargin=2*cm,
                  title="Open Investment - Testes BDD", author="Lucas Gabriel Neres Sant'Ana").build(S)
print("PDF gerado:", out)
