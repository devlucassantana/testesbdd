# language: pt
@alertas @governanca
Funcionalidade: Alertas ESG
  Como gestor de projetos
  Quero acompanhar e tratar alertas de orçamento e voluntariado
  Para agir rapidamente sobre riscos de governança

  Contexto:
    Dado que estou autenticado como "GESTOR"
    E que existe um projeto com orçamento de 1000 reais
    E que existe uma alocação de 900 reais neste projeto

  @positivo @contrato
  Cenário: Listar alertas pendentes
    Quando envio "GET" para "/api/alertas/pendentes"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "alerta"
    E a lista contém um item com "tipoAlerta" igual a "ORCAMENTO_CRITICO"

  @positivo @contrato
  Cenário: Listar alertas de um projeto
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    Então o status da resposta é 200
    E cada item da lista respeita o contrato "alerta"
    E a lista contém um item com "statusAlerta" igual a "PENDENTE"

  @positivo
  Cenário: Marcar alerta como lido
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    E guardo o campo "0.idAlerta" da resposta como "alertaId"
    E envio "PATCH" para "/api/alertas/{{alertaId}}/ler"
    Então o status da resposta é 200
    E o corpo respeita o contrato "alerta"
    E o campo "statusAlerta" é "LIDO"

  @positivo
  Cenário: Resolver alerta
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    E guardo o campo "0.idAlerta" da resposta como "alertaId"
    E envio "PATCH" para "/api/alertas/{{alertaId}}/resolver"
    Então o status da resposta é 200
    E o corpo respeita o contrato "alerta"
    E o campo "statusAlerta" é "RESOLVIDO"

  @negativo
  Cenário: Marcar alerta inexistente como lido retorna 404
    Quando envio "PATCH" para "/api/alertas/999999999/ler"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @negativo
  Cenário: Resolver alerta inexistente retorna 404
    Quando envio "PATCH" para "/api/alertas/999999999/resolver"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @negativo @governanca
  Cenário: Alertas sem autenticação retornam 401
    Dado que não estou autenticado
    Quando envio "GET" para "/api/alertas/pendentes"
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"
