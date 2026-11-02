-- =============================================
-- V1__create_schema.sql
-- Migração inicial: criação de todas as tabelas
-- Gerenciamento de Projetos Sociais ESG
-- Open Investment | FIAP 2TDSOC
-- =============================================

-- USUARIOS (Spring Security)
CREATE TABLE usuarios (
    id_usuario   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome         VARCHAR2(150)  NOT NULL,
    email        VARCHAR2(200)  NOT NULL UNIQUE,
    senha        VARCHAR2(255)  NOT NULL,
    perfil       VARCHAR2(30)   DEFAULT 'USER' NOT NULL,
    ativo        NUMBER(1)      DEFAULT 1 NOT NULL,
    criado_em    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_perfil CHECK (perfil IN ('ADMIN', 'GESTOR', 'USER'))
);

-- PROJETO_SOCIAL
CREATE TABLE projeto_social (
    id_projeto        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_projeto      VARCHAR2(200)  NOT NULL,
    descricao         CLOB,
    ods_vinculacao    VARCHAR2(100),
    data_inicio       DATE           NOT NULL,
    data_fim_prev     DATE,
    orcamento_total   NUMBER(15,2)   NOT NULL,
    verba_utilizada   NUMBER(15,2)   DEFAULT 0 NOT NULL,
    status_projeto    VARCHAR2(20)   DEFAULT 'PLANEJADO' NOT NULL,
    criado_por        NUMBER,
    criado_em         TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    atualizado_em     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_status_proj CHECK (status_projeto IN ('PLANEJADO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO')),
    CONSTRAINT chk_orcamento   CHECK (orcamento_total > 0),
    CONSTRAINT fk_proj_usuario FOREIGN KEY (criado_por) REFERENCES usuarios(id_usuario)
);

-- BENEFICIARIO
CREATE TABLE beneficiario (
    id_beneficiario  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome             VARCHAR2(150) NOT NULL,
    cpf              VARCHAR2(14)  NOT NULL UNIQUE,
    perfil           VARCHAR2(50),
    regiao           VARCHAR2(100),
    data_cadastro    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL
);

-- ALOCACAO_RECURSO
CREATE TABLE alocacao_recurso (
    id_alocacao    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_projeto     NUMBER         NOT NULL,
    descricao      VARCHAR2(300),
    valor          NUMBER(15,2)   NOT NULL,
    tipo_recurso   VARCHAR2(50)   NOT NULL,
    data_alocacao  DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_aloc_projeto  FOREIGN KEY (id_projeto) REFERENCES projeto_social(id_projeto),
    CONSTRAINT chk_valor_aloc   CHECK (valor > 0),
    CONSTRAINT chk_tipo_recurso CHECK (tipo_recurso IN ('FINANCEIRO', 'MATERIAL', 'HUMANO', 'TECNOLOGICO'))
);

-- IMPACTO_SOCIAL
CREATE TABLE impacto_social (
    id_impacto      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_projeto      NUMBER        NOT NULL,
    id_beneficiario NUMBER        NOT NULL,
    tipo_impacto    VARCHAR2(60)  NOT NULL,
    descricao       VARCHAR2(500),
    data_registro   TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_imp_projeto     FOREIGN KEY (id_projeto)     REFERENCES projeto_social(id_projeto),
    CONSTRAINT fk_imp_benefic     FOREIGN KEY (id_beneficiario) REFERENCES beneficiario(id_beneficiario),
    CONSTRAINT chk_tipo_impacto   CHECK (tipo_impacto IN ('CAPACITACAO', 'DOACAO', 'ATENDIMENTO', 'EMPREGO', 'OUTRO'))
);

-- VOLUNTARIO
CREATE TABLE voluntario (
    id_voluntario    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome             VARCHAR2(150) NOT NULL,
    cpf              VARCHAR2(14)  NOT NULL UNIQUE,
    email            VARCHAR2(200) NOT NULL UNIQUE,
    especialidade    VARCHAR2(100),
    status_vol       VARCHAR2(20)  DEFAULT 'ATIVO' NOT NULL,
    data_cadastro    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_status_vol CHECK (status_vol IN ('ATIVO', 'INATIVO'))
);

-- VOLUNTARIO_PROJETO
CREATE TABLE voluntario_projeto (
    id_vol_proj    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_voluntario  NUMBER        NOT NULL,
    id_projeto     NUMBER        NOT NULL,
    data_entrada   DATE          DEFAULT SYSDATE NOT NULL,
    data_saida     DATE,
    horas_atuadas  NUMBER(6,1)   DEFAULT 0,
    CONSTRAINT fk_vp_voluntario FOREIGN KEY (id_voluntario) REFERENCES voluntario(id_voluntario),
    CONSTRAINT fk_vp_projeto    FOREIGN KEY (id_projeto)    REFERENCES projeto_social(id_projeto),
    CONSTRAINT uq_vol_proj      UNIQUE (id_voluntario, id_projeto)
);

-- ALERTA_ESG
CREATE TABLE alerta_esg (
    id_alerta    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_projeto   NUMBER         NOT NULL,
    tipo_alerta  VARCHAR2(50)   NOT NULL,
    mensagem     VARCHAR2(500)  NOT NULL,
    status_alerta VARCHAR2(20)  DEFAULT 'PENDENTE' NOT NULL,
    data_alerta  TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_alert_projeto  FOREIGN KEY (id_projeto) REFERENCES projeto_social(id_projeto),
    CONSTRAINT chk_tipo_alerta   CHECK (tipo_alerta IN ('ORCAMENTO_CRITICO', 'NOVO_VOLUNTARIO', 'PROJETO_INICIADO', 'ORCAMENTO_ESTOURADO')),
    CONSTRAINT chk_status_alerta CHECK (status_alerta IN ('PENDENTE', 'LIDO', 'RESOLVIDO'))
);

-- LOG_ESG (Auditoria)
CREATE TABLE log_esg (
    id_log        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tabela_origem VARCHAR2(60)  NOT NULL,
    operacao      VARCHAR2(30)  NOT NULL,
    id_referencia NUMBER,
    descricao     VARCHAR2(500) NOT NULL,
    id_usuario    NUMBER,
    data_log      TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- =============================================
-- ÍNDICES para performance
-- =============================================
CREATE INDEX idx_projeto_status   ON projeto_social(status_projeto);
CREATE INDEX idx_aloc_projeto     ON alocacao_recurso(id_projeto);
CREATE INDEX idx_impacto_projeto  ON impacto_social(id_projeto);
CREATE INDEX idx_vp_projeto       ON voluntario_projeto(id_projeto);
CREATE INDEX idx_alerta_projeto   ON alerta_esg(id_projeto);
CREATE INDEX idx_log_tabela       ON log_esg(tabela_origem, data_log);
