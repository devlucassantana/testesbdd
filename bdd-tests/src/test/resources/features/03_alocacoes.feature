# language: pt
@alocacoes @compliance
Funcionalidade: Alocação de recursos e controle orçamentário
  Como gestor financeiro
  Quero alocar recursos respeitando o orçamento do projeto
  Para garantir compliance e evitar estouro de verba

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe um projeto com orçamento de 1000 reais

  @positivo @contrato
  Cenário: Alocar valor dentro do orçamento
    Quando envio "POST" para "/api/alocacoes" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "descricao": "Compra de equipamentos", "valor": 400.00, "tipoRecurso": "FINANCEIRO"}
      """
    Então o status da resposta é 201
    E o corpo respeita o contrato "alocacao"
    E o campo "tipoRecurso" é "FINANCEIRO"
    E o campo numérico "valor" é igual a "400"

  @negativo @compliance
  Cenário: Alocação acima do orçamento é bloqueada com 422
    Quando envio "POST" para "/api/alocacoes" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "descricao": "Valor acima do orçamento", "valor": 1500.00, "tipoRecurso": "FINANCEIRO"}
      """
    Então o status da resposta é 422
    E o corpo respeita o contrato "erro"
    E o campo "message" contém "Saldo restante"

  @negativo
  Esquema do Cenário: Rejeitar alocação com dados inválidos (<motivo>)
    Quando envio "POST" para "/api/alocacoes" com o corpo:
      """
      {"idProjeto": {{projetoId}}, "valor": <valor>, "tipoRecurso": "<tipo>"}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.<campo>" existe

    Exemplos:
      | motivo           | valor  | tipo       | campo       |
      | tipo desconhecido | 100.00 | COMBUSTIVEL | tipoRecurso |
      | valor zero       | 0      | FINANCEIRO | valor       |

  @negativo
  Cenário: Alocar em projeto inexistente retorna 404
    Quando envio "POST" para "/api/alocacoes" com o corpo:
      """
      {"idProjeto": 999999999, "valor": 10.00, "tipoRecurso": "FINANCEIRO"}
      """
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @positivo @contrato
  Cenário: Listar alocações de um projeto
    Dado que existe uma alocação de 300 reais neste projeto
    Quando envio "GET" para "/api/alocacoes/projeto/{{projetoId}}"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "alocacao"
    E a lista contém um item com "idAlocacao" igual a "{{alocacaoId}}"

  @positivo @contrato
  Cenário: Buscar alocação por ID
    Dado que existe uma alocação de 300 reais neste projeto
    Quando envio "GET" para "/api/alocacoes/{{alocacaoId}}"
    Então o status da resposta é 200
    E o corpo respeita o contrato "alocacao"

  @negativo
  Cenário: Buscar alocação inexistente retorna 404
    Quando envio "GET" para "/api/alocacoes/999999999"
    Então o status da resposta é 404
    E o corpo respeita o contrato "erro"

  @positivo
  Cenário: Excluir alocação devolve o saldo ao projeto
    Dado que existe uma alocação de 300 reais neste projeto
    Quando envio "DELETE" para "/api/alocacoes/{{alocacaoId}}"
    Então o status da resposta é 204
    Quando envio "GET" para "/api/alocacoes/{{alocacaoId}}"
    Então o status da resposta é 404
    Quando envio "GET" para "/api/projetos/{{projetoId}}"
    Então o campo numérico "verbaUtilizada" é igual a "0"

  @positivo @compliance @governanca
  Cenário: Uso de 80% do orçamento gera alerta e marca o projeto como crítico
    Dado que existe uma alocação de 850 reais neste projeto
    Quando envio "GET" para "/api/alertas/projeto/{{projetoId}}"
    Então o status da resposta é 200
    E cada item da lista respeita o contrato "alerta"
    E a lista contém um item com "tipoAlerta" igual a "ORCAMENTO_CRITICO"
    Quando envio "GET" para "/api/projetos/orcamento-critico"
    Então o status da resposta é 200
    E cada item da lista respeita o contrato "projeto"
    E a lista contém um item com "idProjeto" igual a "{{projetoId}}"
