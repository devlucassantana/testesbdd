# language: pt
@autenticacao
Funcionalidade: Autenticação e controle de acesso
  Como usuário da plataforma Open Investment
  Quero me autenticar com e-mail e senha
  Para acessar apenas os recursos permitidos ao meu perfil (governança e segurança)

  @positivo @contrato
  Cenário: Login com credenciais válidas retorna token JWT
    Dado que não estou autenticado
    Quando envio "POST" para "/api/auth/login" com o corpo:
      """
      {"email": "admin@openinvestment.com", "senha": "Admin@123"}
      """
    Então o status da resposta é 200
    E o corpo respeita o contrato "auth"
    E o campo "tipo" é "Bearer"
    E o campo "perfil" é "ADMIN"

  @positivo
  Cenário: Login do perfil GESTOR retorna o perfil correto
    Dado que não estou autenticado
    Quando envio "POST" para "/api/auth/login" com o corpo:
      """
      {"email": "gestor@openinvestment.com", "senha": "Gestor@123"}
      """
    Então o status da resposta é 200
    E o corpo respeita o contrato "auth"
    E o campo "perfil" é "GESTOR"

  @negativo
  Cenário: Login com senha incorreta é recusado com 401
    Dado que não estou autenticado
    Quando envio "POST" para "/api/auth/login" com o corpo:
      """
      {"email": "admin@openinvestment.com", "senha": "senha-errada"}
      """
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"
    E o campo numérico "status" é igual a "401"

  @negativo
  Cenário: Login com e-mail inexistente é recusado com 401
    Dado que não estou autenticado
    Quando envio "POST" para "/api/auth/login" com o corpo:
      """
      {"email": "naoexiste@openinvestment.com", "senha": "Qualquer@123"}
      """
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"

  @negativo
  Cenário: Login sem preencher os campos retorna erro de validação
    Dado que não estou autenticado
    Quando envio "POST" para "/api/auth/login" com o corpo:
      """
      {}
      """
    Então o status da resposta é 400
    E o corpo respeita o contrato "erro-validacao"
    E o campo "campos.email" existe
    E o campo "campos.senha" existe

  @negativo @governanca
  Cenário: Rota protegida sem token retorna 401
    Dado que não estou autenticado
    Quando envio "GET" para "/api/projetos"
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"

  @negativo @governanca
  Cenário: Token JWT malformado é rejeitado com 401
    Dado que uso o token inválido "abc.def.ghi"
    Quando envio "GET" para "/api/projetos"
    Então o status da resposta é 401
    E o corpo respeita o contrato "erro"
