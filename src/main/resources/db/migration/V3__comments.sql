-- =============================================
-- V3__indexes_and_constraints.sql
-- Constraints adicionais e comentários nas tabelas
-- =============================================

-- Comentários nas tabelas para documentação do schema
COMMENT ON TABLE projeto_social    IS 'Projetos sociais vinculados aos ODS da ONU — entidade central do sistema ESG';
COMMENT ON TABLE beneficiario      IS 'Cadastro de pessoas físicas atendidas pelos projetos sociais';
COMMENT ON TABLE alocacao_recurso  IS 'Registro de alocações financeiras e materiais por projeto';
COMMENT ON TABLE impacto_social    IS 'Documentação dos impactos gerados por projeto e beneficiário';
COMMENT ON TABLE voluntario        IS 'Cadastro de voluntários com especialidade e status de participação';
COMMENT ON TABLE voluntario_projeto IS 'Vínculo entre voluntários e projetos com controle de horas';
COMMENT ON TABLE alerta_esg        IS 'Alertas automáticos de governança gerados pelo sistema';
COMMENT ON TABLE log_esg           IS 'Trilha completa de auditoria de todas as operações automatizadas';
COMMENT ON TABLE usuarios          IS 'Usuários do sistema com perfis de acesso (ADMIN, GESTOR, USER)';
