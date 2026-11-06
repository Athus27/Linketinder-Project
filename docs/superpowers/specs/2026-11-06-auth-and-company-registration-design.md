# Login e cadastro de empresa — desenho

## Objetivo

Concluir a integração do Linketinder: cadastrar empresas pela API e permitir que candidato ou empresa entre usando e-mail e senha, vendo no cabeçalho seu nome e tipo de perfil.

## Limites

- Preservar os perfis atuais em `Candidate` e `Company`, ligados à identidade comum em `User`.
- Garantir unicidade de e-mail sem diferenciar maiúsculas/minúsculas nem espaços nas pontas; antes da migração, confirmar ausência de colisões.
- Verificar senha no backend com `crypt` e nunca devolver ou registrar senha/hash.
- A interface mantém o usuário autenticado apenas em memória e mostra o menu correspondente ao perfil.
- Não implementar sessão/token nem autorização de endpoints nesta etapa; o login confirma credenciais para a experiência de interface, mas não protege recursos da API.
- Não criar o perfil Recrutador agora. A futura extensão poderá adicionar outro perfil ligado a `User` e incluir seu tipo no resolvedor de perfil.

## Abordagem escolhida

Reaproveitar `User` como identidade e as tabelas de perfil existentes para resolver `candidate` ou `company`. O banco mantém a regra como índice único funcional em `lower(trim(email))`; uma migração versionada protege bancos existentes, e o schema base recebe a mesma regra para instalações novas. A API adiciona `POST /api/auth/login`; o cadastro de empresa usa o endpoint POST já existente.

Essa abordagem evita duplicar credenciais ou criar uma coluna de tipo que possa divergir dos perfis. Uma tabela central de roles ou um sistema completo de sessão teria escopo maior e não é requisito desta integração.

## Critérios de aceite

- Cadastro de empresa do formulário chama `POST /api/empresas` com os campos esperados, incluindo senha.
- Login válido retorna apenas `id`, `nome` e `tipo`; inválido retorna 401 sem revelar se e-mail ou senha estava errado.
- E-mail é comparado sem sensibilidade a caixa/espaços e não pode ser duplicado entre candidatos e empresas.
- Frontend mostra nome e menu do tipo de perfil após login; sair limpa o estado em memória.
- Testes de backend e frontend, build e integração com PostgreSQL passam.
- READMEs e `requests.http` refletem os endpoints e a limitação de ausência de sessão/autorização.
