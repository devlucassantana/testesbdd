# language: pt
@contratos
Funcionalidade: Contratos das APIs de consulta (JSON Schema)
  Como consumidor da API
  Quero que as listagens respeitem um contrato estável
  Para integrar sistemas sem quebras inesperadas

  @contrato @positivo
  Esquema do Cenário: Listagem <endpoint> respeita o contrato <contrato>
    Dado que estou autenticado como "ADMIN"
    Quando envio "GET" para "<endpoint>"
    Então o status da resposta é 200
    E o corpo é uma lista
    E cada item da lista respeita o contrato "<contrato>"

    Exemplos:
      | endpoint                        | contrato     |
      | /api/projetos                   | projeto      |
      | /api/projetos/orcamento-critico | projeto      |
      | /api/beneficiarios              | beneficiario |
      | /api/voluntarios                | voluntario   |
      | /api/alertas/pendentes          | alerta       |
