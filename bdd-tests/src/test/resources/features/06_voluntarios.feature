# language: pt
@voluntarios @social
Funcionalidade: Gestão de voluntários
  Como coordenador de voluntariado
  Quero cadastrar voluntários e vinculá-los a projetos
  Para ampliar o impacto social com rastreabilidade

  Contexto:
    Dado que estou autenticado como "ADMIN"

  @positivo @contrato
  Cenário: Listar voluntários
    Quando envio "GET" para "/api/voluntarios"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "voluntario"

  @positivo @contrato
  Cenário: Cadastrar voluntário com dados válidos
    Quando envio "POST" para "/api/voluntarios" com o corpo:
      """
      {"nome": "Voluntario BDD {{unique}}", "cpf": "{{cpf}}", "email": "{{email}}", "especialidade": "Pedagogia"}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "voluntario"
    E o campo "statusVol" é "ATIVO"

  @negativo @governanca
  Cenário: CPF duplicado é recusado com 422
    Dado que existe um voluntário cadastrado
    Quando envio "POST" para "/api/voluntarios" com o corpo:
      """
      {"nome": "Outro Voluntario", "cpf": "{{cpf}}", "email": "outro{{email}}"}
      """
    Então o status da resposta é 422
    E o corpo respeita o contrato "erro"

  @negativo
  Cenário: E-mail inválido retorna erro de validação
    Quando envio "POST" para "/api/voluntarios" com o corpo:
      """
      {"nome": "Voluntario Invalido", "cpf": "{{cpf}}", "email": "email-sem-arroba"}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.email" existe

  @positivo @contrato
  Cenário: Buscar voluntário por ID
    Dado que existe um voluntário cadastrado
    Quando envio "GET" para "/api/voluntarios/{{voluntarioId}}"
    Então o status da resposta é 200
    E o corpo respeita o contrato "voluntario"
    E o campo "idVoluntario" é "{{voluntarioId}}"

  @negativo
  Cenário: Buscar voluntário inexistente retorna 404
    Quando envio "GET" para "/api/voluntarios/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @positivo
  Cenário: Atualizar voluntário
    Dado que existe um voluntário cadastrado
    Quando envio "PUT" para "/api/voluntarios/{{voluntarioId}}" com o corpo:
      """
      {"nome": "Voluntario Atualizado {{unique}}", "cpf": "{{cpf}}", "email": "{{email}}", "especialidade": "Engenharia"}
      """
    Então o status da resposta é 200
    E o corpo respeita o contrato "voluntario"
    E o campo "especialidade" é "Engenharia"

  @positivo
  Cenário: Inativar voluntário
    Dado que existe um voluntário cadastrado
    Quando envio "PATCH" para "/api/voluntarios/{{voluntarioId}}/inativar"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/voluntarios/{{voluntarioId}}"
    Então o campo "statusVol" é "INATIVO"

  @positivo @governanca
  Cenário: Vincular voluntário a um projeto gera alerta
    Dado que existe um voluntário cadastrado
    E que existe um projeto com orçamento de 1000 reais
    Quando envio "POST" para "/api/voluntarios/{{voluntarioId}}/vincular-projeto/{{projetoId}}"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    Então a lista contém um item com "tipoAlerta" igual a "NOVO_VOLUNTARIO"

  @negativo
  Cenário: Vincular voluntário a projeto inexistente retorna 404
    Dado que existe um voluntário cadastrado
    Quando envio "POST" para "/api/voluntarios/{{voluntarioId}}/vincular-projeto/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"
