# language: pt
@auditoria @governanca
Funcionalidade: Trilha de auditoria
  Como auditor (perfil ADMIN)
  Quero consultar o log de operações do sistema
  Para garantir integridade, rastreabilidade e conformidade

  @positivo @contrato
  Cenário: ADMIN consulta o log de auditoria paginado
    Dado que estou autenticado como "ADMIN"
    Quando envio "GET" para "/api/logs?page=0&size=5"
    Então o status da resposta é 200
    E o corpo respeita o contrato "pagina-logs"

  @positivo @contrato
  Cenário: Criar projeto registra operação INSERT na auditoria
    Dado que estou autenticado como "ADMIN"
    E que existe um projeto com orçamento de 1000 reais
    Quando envio "GET" para "/api/logs/tabela/projeto_social"
    Então o status da resposta é 200
    E o corpo respeita o contrato "pagina-logs"
    E a lista em "content" contém um item com "idReferencia" igual a "{{projetoId}}"
    E a lista em "content" contém um item com "operacao" igual a "INSERT"

  @positivo
  Cenário: Vincular voluntário a projeto fica registrado na auditoria
    Dado que estou autenticado como "ADMIN"
    E que existe um voluntário cadastrado
    E que existe um projeto com orçamento de 1000 reais
    Quando envio "POST" para "/api/voluntarios/{{voluntarioId}}/vincular-projeto/{{projetoId}}"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/logs/tabela/voluntario_projeto"
    Então o status da resposta é 200
    E a lista em "content" contém um item com "idReferencia" igual a "{{projetoId}}"

  @negativo
  Cenário: GESTOR não pode consultar o log de auditoria
    Dado que estou autenticado como "GESTOR"
    Quando envio "GET" para "/api/logs"
    Então o status da resposta é 403
    E o corpo respeita o contrato "erro"

  @negativo
  Cenário: Log de auditoria sem autenticação retorna 401
    Dado que não estou autenticado
    Quando envio "GET" para "/api/logs/tabela/projeto_social"
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"
