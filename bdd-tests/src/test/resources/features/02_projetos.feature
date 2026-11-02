# language: pt
@projetos
Funcionalidade: Gestão de projetos sociais
  Como gestor de projetos sociais ESG
  Quero cadastrar, consultar, atualizar e encerrar projetos
  Para acompanhar o investimento social com rastreabilidade

  Contexto:
    Dado que estou autenticado como "ADMIN"

  @positivo @contrato
  Cenário: Listar projetos cadastrados
    Quando envio "GET" para "/api/projetos"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "projeto"

  @positivo @contrato
  Cenário: Criar projeto com dados válidos
    Quando envio "POST" para "/api/projetos" com o corpo:
      """
      {"nomeProjeto": "Projeto BDD {{unique}}", "descricao": "Horta comunitaria", "odsVinculacao": "ODS 2 - Fome Zero", "dataInicio": "2026-03-01", "dataFimPrev": "2026-12-31", "orcamentoTotal": 1000.00}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "projeto"
    E o campo "statusProjeto" é "PLANEJADO"
    E o campo numérico "verbaUtilizada" é igual a "0"
    E o campo numérico "saldoDisponivel" é igual a "1000"

  @negativo
  Esquema do Cenário: Rejeitar projeto com dados inválidos (<motivo>)
    Quando envio "POST" para "/api/projetos" com o corpo:
      """
      {"nomeProjeto": "<nome>", "dataInicio": "2026-03-01", "orcamentoTotal": <orcamento>}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.<campo>" existe

    Exemplos:
      | motivo             | nome      | orcamento | campo          |
      | nome em branco     |           | 1000.00   | nomeProjeto    |
      | orçamento zero     | Projeto X | 0         | orcamentoTotal |
      | orçamento negativo | Projeto X | -50.00    | orcamentoTotal |

  @positivo @contrato
  Cenário: Buscar projeto por ID
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "GET" para "/api/projetos/{{projetoId}}"
    Então o status da resposta é 200
    E o corpo respeita o contrato "projeto"
    E o campo "idProjeto" é "{{projetoId}}"

  @negativo
  Cenário: Buscar projeto inexistente retorna 404
    Quando envio "GET" para "/api/projetos/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"
    E o campo numérico "status" é igual a "404"

  @positivo
  Cenário: Filtrar projetos por status
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "GET" para "/api/projetos?status=PLANEJADO"
    Então o status da resposta é 200
    E cada item da lista respeita o contrato "projeto"
    E a lista contém um item com "idProjeto" igual a "{{projetoId}}"

  @positivo
  Cenário: Atualizar dados de um projeto
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "PUT" para "/api/projetos/{{projetoId}}" com o corpo:
      """
      {"nomeProjeto": "Projeto BDD Atualizado {{unique}}", "descricao": "Descricao revisada", "odsVinculacao": "ODS 4", "dataInicio": "2026-04-01", "dataFimPrev": "2027-03-31", "orcamentoTotal": 2000.00}
      """
    Então o status da resposta é 200
    E o corpo respeita o contrato "projeto"
    E o campo "nomeProjeto" é "Projeto BDD Atualizado {{unique}}"
    E o campo numérico "orcamentoTotal" é igual a "2000"

  @positivo
  Cenário: Alterar o status do projeto para EM_ANDAMENTO
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "PATCH" para "/api/projetos/{{projetoId}}/status?status=EM_ANDAMENTO"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/projetos/{{projetoId}}"
    Então o status da resposta é 200
    E o campo "statusProjeto" é "EM_ANDAMENTO"

  @negativo
  Cenário: Status inexistente viola regra de negócio (422)
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "PATCH" para "/api/projetos/{{projetoId}}/status?status=INEXISTENTE"
    Então o status da resposta é 422
    E o corpo respeita o contrato "erro"
    E o campo numérico "status" é igual a "422"

  @positivo
  Cenário: ADMIN exclui projeto sem registros dependentes
    Dado que existe um projeto com orçamento de 1000 reais
    Quando envio "DELETE" para "/api/projetos/{{projetoId}}"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/projetos/{{projetoId}}"
    Então o status da resposta é 404

  @negativo @governanca
  Cenário: GESTOR não tem permissão para excluir projeto
    Dado que existe um projeto com orçamento de 1000 reais
    E que estou autenticado como "GESTOR"
    Quando envio "DELETE" para "/api/projetos/{{projetoId}}"
    Então o status da resposta é 403
    E o corpo respeita o contrato "erro"

  @positivo @governanca
  Cenário: GESTOR pode criar projeto
    Dado que estou autenticado como "GESTOR"
    Quando envio "POST" para "/api/projetos" com o corpo:
      """
      {"nomeProjeto": "Projeto Gestor {{unique}}", "dataInicio": "2026-03-01", "orcamentoTotal": 500.00}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "projeto"
