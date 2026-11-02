# language: pt
@impactos @social
Funcionalidade: Registro de impacto social
  Como responsável pelo pilar Social do ESG
  Quero registrar os beneficiários atendidos por cada projeto
  Para medir e comprovar o impacto social gerado

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe um projeto com orçamento de 1000 reais
    E que existe um beneficiário cadastrado

  @positivo @contrato
  Cenário: Registrar impacto inicia automaticamente o projeto
    Quando envio "POST" para "/api/impactos" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "idBeneficiario": {{beneficiarioId}}, "tipoImpacto": "CAPACITACAO", "descricao": "Curso de informatica"}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "impacto"
    E o campo "tipoImpacto" é "CAPACITACAO"
    Quando envio "GET" para "/api/projetos/{{projetoId}}"
    Então o campo "statusProjeto" é "EM_ANDAMENTO"
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    Então a lista contém um item com "tipoAlerta" igual a "PROJETO_INICIADO"

  @negativo
  Cenário: Tipo de impacto inválido retorna erro de validação
    Quando envio "POST" para "/api/impactos" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "idBeneficiario": {{beneficiarioId}}, "tipoImpacto": "INVALIDO"}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.tipoImpacto" existe

  @negativo
  Cenário: Beneficiário inexistente retorna 404
    Quando envio "POST" para "/api/impactos" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "idBeneficiario": 999999999, "tipoImpacto": "DOACAO"}
      """
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @negativo
  Cenário: Projeto inexistente retorna 404
    Quando envio "POST" para "/api/impactos" com o corpo:
      """
      {"idProjeto": 999999999, "idBeneficiario": {{beneficiarioId}}, "tipoImpacto": "DOACAO"}
      """
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @positivo @contrato
  Cenário: Listar impactos de um projeto
    Dado que existe um impacto registrado neste projeto
    Quando envio "GET" para "/api/impactos/projeto/{{projetoId}}"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "impacto"
    E a lista contém um item com "idImpacto" igual a "{{impactoId}}"

  @positivo @contrato
  Cenário: Contar impactos de um projeto
    Dado que existe um impacto registrado neste projeto
    Quando envio "GET" para "/api/impactos/projeto/{{projetoId}}/contagem"
    Então o status da resposta é 200
    E o corpo respeita o contrato "contagem"
    E o campo numérico "totalImpactos" é igual a "1"

  @negativo
  Cenário: Listar impactos de projeto inexistente retorna 404
    Quando envio "GET" para "/api/impactos/projeto/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"
