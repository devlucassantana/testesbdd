# language: pt
@beneficiarios @social
Funcionalidade: Cadastro de beneficiários
  Como responsável pelo pilar Social do ESG
  Quero manter o cadastro de pessoas atendidas
  Para garantir inclusão e evitar registros duplicados

  Contexto:
    Dado que estou autenticado como "GESTOR"

  @positivo @contrato
  Cenário: Listar beneficiários
    Quando envio "GET" para "/api/beneficiarios"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "beneficiario"

  @positivo @contrato
  Cenário: Cadastrar beneficiário com dados válidos
    Quando envio "POST" para "/api/beneficiarios" com o corpo:
      """
      {"nome": "Beneficiario BDD {{unique}}", "cpf": "{{cpf}}", "perfil": "Jovem 18-25", "regiao": "Zona Leste - SP"}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "beneficiario"
    E o campo "cpf" é "{{cpf}}"

  @negativo @governanca
  Cenário: CPF duplicado é recusado com 422
    Dado que existe um beneficiário cadastrado
    Quando envio "POST" para "/api/beneficiarios" com o corpo:
      """
      {"nome": "Outro Beneficiario", "cpf": "{{cpf}}"}
      """
    Então o status da resposta é 422
    E o corpo respeita o contrato "erro"
    E o campo "message" contém "CPF"

  @negativo
  Cenário: Cadastro sem nome retorna erro de validação
    Quando envio "POST" para "/api/beneficiarios" com o corpo:
      """
      {"nome": "", "cpf": "{{cpf}}"}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.nome" existe

  @positivo @contrato
  Cenário: Buscar beneficiário por ID
    Dado que existe um beneficiário cadastrado
    Quando envio "GET" para "/api/beneficiarios/{{beneficiarioId}}"
    Então o status da resposta é 200
    E o corpo respeita o contrato "beneficiario"
    E o campo "idBeneficiario" é "{{beneficiarioId}}"

  @negativo
  Cenário: Buscar beneficiário inexistente retorna 404
    Quando envio "GET" para "/api/beneficiarios/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @positivo
  Cenário: Atualizar beneficiário
    Dado que existe um beneficiário cadastrado
    Quando envio "PUT" para "/api/beneficiarios/{{beneficiarioId}}" com o corpo:
      """
      {"nome": "Beneficiario Atualizado {{unique}}", "cpf": "{{cpf}}", "perfil": "Adulto 26-45", "regiao": "Zona Sul - SP"}
      """
    Então o status da resposta é 200
    E o corpo respeita o contrato "beneficiario"
    E o campo "regiao" é "Zona Sul - SP"

  @positivo
  Cenário: Excluir beneficiário sem impactos vinculados
    Dado que existe um beneficiário cadastrado
    Quando envio "DELETE" para "/api/beneficiarios/{{beneficiarioId}}"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/beneficiarios/{{beneficiarioId}}"
    Então o status da resposta é 404
