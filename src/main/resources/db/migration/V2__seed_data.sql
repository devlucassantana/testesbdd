-- =============================================
-- V2__seed_data.sql
-- Dados iniciais: usuários + projetos de exemplo
-- NOTA: Flyway faz o commit automaticamente — não usar COMMIT explícito
-- =============================================

-- Usuário ADMIN (senha: Admin@123)
INSERT INTO usuarios (nome, email, senha, perfil)
VALUES ('Administrador', 'admin@openinvestment.com',
        '$2a$10$kRbOFwAn3/7WoT/D.vCgV.Zaw6FsoVAiLFKU3mc.FmGWZO8wfacC2', 'ADMIN');

-- Usuário GESTOR (senha: Gestor@123)
INSERT INTO usuarios (nome, email, senha, perfil)
VALUES ('Lucas Gestor', 'gestor@openinvestment.com',
        '$2a$10$k1WUm.8662E5qIZWByIe/uvkltAEumE4LUynQ6UbnXuaSAzCmSPsa', 'GESTOR');

-- Projetos Sociais de exemplo
INSERT INTO projeto_social (nome_projeto, descricao, ods_vinculacao, data_inicio, data_fim_prev, orcamento_total, criado_por)
VALUES ('Capacitacao Digital Periferia',
        'Oferecer cursos de informatica e programacao para jovens de baixa renda em SP',
        'ODS 4 - Educacao de Qualidade', DATE '2026-01-01', DATE '2026-12-31', 150000.00, 1);

INSERT INTO projeto_social (nome_projeto, descricao, ods_vinculacao, data_inicio, data_fim_prev, orcamento_total, criado_por)
VALUES ('Hortas Comunitarias Urbanas',
        'Implantacao de hortas em comunidades carentes para seguranca alimentar',
        'ODS 2 - Fome Zero', DATE '2026-02-01', DATE '2026-08-31', 80000.00, 2);

INSERT INTO projeto_social (nome_projeto, descricao, ods_vinculacao, data_inicio, data_fim_prev, orcamento_total, criado_por)
VALUES ('Apoio a Empreendedores Sociais',
        'Mentoria e microcredito para empreendedores em vulnerabilidade social',
        'ODS 8 - Trabalho Digno e Crescimento', DATE '2026-03-01', DATE '2026-12-31', 200000.00, 2);

-- Beneficiários de exemplo
INSERT INTO beneficiario (nome, cpf, perfil, regiao)
VALUES ('Maria Silva', '111.222.333-44', 'Jovem 18-25', 'Zona Leste - SP');

INSERT INTO beneficiario (nome, cpf, perfil, regiao)
VALUES ('Joao Ferreira', '555.666.777-88', 'Adulto 26-45', 'Zona Norte - SP');

INSERT INTO beneficiario (nome, cpf, perfil, regiao)
VALUES ('Ana Costa', '999.888.777-66', 'Jovem 18-25', 'Zona Sul - SP');

-- Voluntários de exemplo
INSERT INTO voluntario (nome, cpf, email, especialidade)
VALUES ('Carlos Mendes', '123.456.789-00', 'carlos@voluntario.com', 'Tecnologia da Informacao');

INSERT INTO voluntario (nome, cpf, email, especialidade)
VALUES ('Patricia Souza', '987.654.321-00', 'patricia@voluntario.com', 'Pedagogia');
