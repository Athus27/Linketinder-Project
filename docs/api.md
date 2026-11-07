# Contrato da API

Base local: http://localhost:8080. Requisições com corpo usam JSON e
Content-Type: application/json. Os exemplos abaixo são ilustrativos, não
credenciais ou cadastros existentes. Substitua os placeholders antes de testar.

## Endpoints e permissões

| Método | Endpoint | Acesso |
|---|---|---|
| GET | /api/candidatos | Público, com projeção conforme sessão/match |
| GET | /api/candidatos/{id} | Público, com projeção conforme sessão/match |
| POST | /api/candidatos | Cadastro público |
| DELETE | /api/candidatos/{id} | Somente o próprio candidato autenticado |
| GET | /api/empresas | Público, com projeção conforme sessão/match |
| POST | /api/empresas | Cadastro público |
| GET | /api/vagas | Listagem pública |
| GET | /api/vagas?minhas=true | Empresa ou recrutador; somente vagas da empresa vinculada |
| POST | /api/vagas | Somente empresa autenticada |
| POST | /api/auth/login | Público |
| POST | /api/auth/logout | Revogação idempotente do token enviado |
| GET, POST | /api/recrutadores | Somente empresa; gestão dos próprios recrutadores |
| DELETE | /api/recrutadores/{id} | Somente empresa vinculada; desativação lógica |
| POST | /api/curtidas | Candidato, empresa ou recrutador autenticado |
| GET | /api/matches | Candidato, empresa ou recrutador; apenas seus matches |

Listagens retornam arrays; ausência de registros retorna []. Os GETs públicos
não exigem token, mas um Authorization inválido não é tratado como visitante.
Menus do frontend não substituem as verificações no servidor.

## Login e sessão

~~~http
POST /api/auth/login
Content-Type: application/json

{
  "email": "recrutador@example.test",
  "senha": "<SENHA_DO_CADASTRO>"
}
~~~

Exemplo de resposta 200 de recrutador:

~~~json
{
  "token": "<TOKEN_RETORNADO_PELO_LOGIN>",
  "id": 3,
  "nome": "Recrutador Exemplo",
  "tipo": "recruiter",
  "idEmpresa": 6
}
~~~

Os tipos são candidate, company e recruiter. Empresa também recebe idEmpresa;
candidato não recebe esse campo. Senha, hash e documentos não são retornados.

O token é opaco, gerado pelo servidor e armazenado em memória com validade
de quatro horas. Envie-o nas operações protegidas:

~~~http
GET /api/recrutadores
Authorization: Bearer <TOKEN_DA_EMPRESA>
~~~

O servidor resolve a sessão e revalida o usuário no banco. Ator e empresa são
derivados dessa identidade, não de seletores ou IDs livres enviados pelo cliente.
Expiração, revogação e desativação do recrutador impedem o acesso; reiniciar
a API invalida as sessões existentes.

~~~http
POST /api/auth/logout
Authorization: Bearer <TOKEN>
~~~

Logout retorna 204 sem corpo e pode ser repetido, inclusive sem token válido.
Credenciais incorretas no login retornam 401; corpo inválido ou campos ausentes,
400. Erros usam mensagem genérica sem revelar dados do cadastro.

## Candidatos

### Consulta e anonimato

GET /api/candidatos retorna, antes do match:

~~~json
[
  {
    "id": 1,
    "competencias": ["Groovy", "Java"],
    "formacao": "Tecnologia"
  }
]
~~~

GET /api/candidatos/1 retorna um objeto com a mesma projeção. Nome, sobrenome,
e-mail, descrição livre, documentos, nascimento e endereço não são liberados
nessa consulta anônima. Formacao pode ser null.

Para uma empresa que possui match com esse candidato em pelo menos uma de
suas vagas, ou um recrutador ativo dessa empresa, a projeção também contém:

~~~json
{
  "id": 1,
  "competencias": ["Groovy", "Java"],
  "formacao": "Tecnologia",
  "nome": "Pessoa Exemplo",
  "sobrenome": "Teste",
  "email": "candidato@example.test",
  "descricao": "Descrição profissional fictícia"
}
~~~

A liberação é específica da empresa com match, não pública. CPF, nascimento,
endereço, senha e hash continuam ausentes. ID inválido retorna 400; candidato
não encontrado, 404.

### Cadastro

~~~http
POST /api/candidatos
Content-Type: application/json

{
  "nome": "Pessoa Exemplo",
  "sobrenome": "Teste",
  "dataNascimento": "15/01/2002",
  "email": "candidato@example.test",
  "cpf": "<CPF_FICTICIO_UNICO>",
  "pais": "Brasil",
  "cep": "01010-000",
  "descricao": "Descrição profissional fictícia",
  "formacao": "Tecnologia",
  "senha": "<SENHA_COM_6_OU_MAIS_CARACTERES>",
  "competencias": ["Groovy", "Java"]
}
~~~

A data de cadastro usa dd/MM/yyyy. País e CEP são obrigatórios; a senha
mantém o mínimo de seis caracteres. O DAO grava User, Candidate e vínculos
de competências em transação. Sucesso retorna 201:

~~~json
{"mensagem":"Candidato cadastrado com sucesso"}
~~~

Conflito de unicidade retorna 409 com mensagem genérica.

### Exclusão

~~~http
DELETE /api/candidatos/1
Authorization: Bearer <TOKEN_DO_PROPRIO_CANDIDATO>
~~~

Exclusão é destrutiva e retorna 204 sem corpo quando concluída. Sem sessão
válida, retorna 401; candidato tentando excluir outro ID ou outro perfil,
403. Confira o ID antes de testar. A remoção abrange os vínculos tratados
pelo DAO.

## Empresas

GET /api/empresas retorna [{ "id": 6 }] antes do match. Para o candidato com
match com uma vaga dessa empresa, também pode retornar nome e email.
CNPJ, endereço e senha/hash não são expostos.

~~~http
POST /api/empresas
Content-Type: application/json

{
  "nome": "Empresa Exemplo",
  "email": "empresa@example.test",
  "cnpj": "<CNPJ_FICTICIO_UNICO>",
  "pais": "Brasil",
  "cep": "01010-000",
  "descricao": "Empresa fictícia de tecnologia",
  "senha": "<SENHA_COM_6_OU_MAIS_CARACTERES>"
}
~~~

País e CEP são obrigatórios; senha mínima de seis caracteres. Sucesso retorna
201 com mensagem de cadastro. E-mail é único considerando lower(trim(email));
conflitos de unicidade retornam 409.

## Recrutadores

A empresa autenticada cadastra o recrutador vinculado a ela. Não existe
cadastro público nem escolha livre de empresa no frontend.

~~~http
POST /api/recrutadores
Authorization: Bearer <TOKEN_DA_EMPRESA>
Content-Type: application/json

{
  "nome": "Recrutador Exemplo",
  "area": "TI",
  "descricao": "Perfil fictício para demonstração",
  "email": "recrutador@example.test",
  "cpf": "<CPF_FICTICIO_VALIDO_E_UNICO>",
  "dataNascimento": "1990-01-01",
  "senha": "<SENHA_RECRUTADOR>"
}
~~~

Substitua o CPF por um valor sintético válido e a senha por um valor com
8–72 bytes UTF-8, ao menos uma letra e um número. Nome tem 2–120 caracteres,
área 2–100 e descrição 1–2000, após remoção de espaços externos. O e-mail
é normalizado e único; CPF rejeita sequências repetidas e dígitos verificadores
incorretos. Nascimento usa yyyy-MM-dd, com data real e idade mínima de 18 anos.
A senha é armazenada com hash pgcrypto, nunca em texto puro.

POST retorna 201 com a projeção de gestão; GET retorna uma lista:

~~~json
[
  {
    "id": 3,
    "nome": "Recrutador Exemplo",
    "email": "recrutador@example.test",
    "area": "TI",
    "descricao": "Perfil fictício para demonstração",
    "idEmpresa": 6,
    "ativo": true
  }
]
~~~

CPF, nascimento, senha e hash não aparecem. DELETE /api/recrutadores/3
desativa logicamente e retorna 204; o cadastro permanece e o recrutador
não pode autenticar nem continuar usando tokens anteriores. ID inexistente
ou de outra empresa retorna 404. Candidato e recrutador recebem 403 na gestão.
Campos inválidos retornam 400; conflitos, 409.

## Vagas

GET /api/vagas retorna:

~~~json
[
  {
    "id": 1,
    "idEmpresa": 6,
    "titulo": "Desenvolvedor Groovy",
    "descricao": "Desenvolvimento de APIs",
    "competencias": ["Groovy", "Java"]
  }
]
~~~

Não inclui nome/e-mail da empresa, CNPJ ou localVaga. Empresa e recrutador
consultam suas vagas com GET /api/vagas?minhas=true e Bearer; nesse endpoint,
visitante e candidato recebem 401 na implementação atual.

~~~http
POST /api/vagas
Authorization: Bearer <TOKEN_DA_EMPRESA>
Content-Type: application/json

{
  "titulo": "Desenvolvedor Groovy",
  "descricao": "Desenvolvimento de APIs",
  "localVaga": "Remoto",
  "competencias": ["Groovy", "Java"]
}
~~~

A empresa vem da sessão. Título, descrição e local são obrigatórios;
competencias deve ser uma coleção de textos não vazios (a coleção pode estar
vazia). Sucesso retorna 201 com mensagem de cadastro. Recrutador não pode
cadastrar vaga e recebe 403. Enviar idEmpresa diferente da sessão também
retorna 403.

## Curtidas e matches

O candidato informa somente a vaga:

~~~http
POST /api/curtidas
Authorization: Bearer <TOKEN_DO_CANDIDATO>
Content-Type: application/json

{"idVaga":1}
~~~

Empresa ou recrutador informa a vaga própria e o candidato:

~~~http
POST /api/curtidas
Authorization: Bearer <TOKEN_DA_EMPRESA_OU_RECRUTADOR>
Content-Type: application/json

{"idVaga":1,"idCandidato":1}
~~~

Resposta 200:

~~~json
{"sucesso":true,"jaCurtiu":false,"deuMatch":false}
~~~

A curtida do recrutador representa sua empresa. Repetir, inclusive com outro
recrutador da mesma empresa, não duplica o voto; a primeira autoria é preservada.
O match ocorre quando candidato e empresa curtem o mesmo par candidato-vaga.
IDs de ator forjados são recusados; recurso inexistente ou vaga alheia recebe 404.

GET /api/matches exige Bearer. Para o candidato, retorna os contatos da empresa:

~~~json
[
  {
    "idCandidato": 1,
    "idVaga": 1,
    "dataMatch": "2026-11-07T12:00:00Z",
    "empresa": {
      "id": 6,
      "nome": "Empresa Exemplo",
      "email": "empresa@example.test"
    }
  }
]
~~~

Para empresa/recrutador, cada item possui candidato, com id, nome, sobrenome,
email e descricao, em vez de empresa. Só são retornados os matches do ator
ou empresa autorizada. Data legada ausente permanece null; a interface exibe
“Data não registrada”. Dados pessoais do recrutador não são liberados ao
candidato; documentos, nascimento, senha e hash nunca são retornados.

## Status e limites

| Status | Significado |
|---|---|
| 200 | Consulta, login ou curtida concluída |
| 201 | Cadastro concluído |
| 204 | Logout, exclusão, desativação ou preflight aceito; sem corpo |
| 400 | JSON, campos ou IDs inválidos |
| 401 | Credenciais ou sessão inválidas/ausentes |
| 403 | Papel/ator não permitido ou CORS recusado |
| 404 | Rota/recurso não encontrado ou vínculo alheio ocultado |
| 405 | Método não permitido, conforme o handler |
| 409 | Conflito de unicidade |
| 500 | Falha interna |

Exemplo de erro de autorização:

~~~json
{"erro":"Papel não permitido","codigo":"FORBIDDEN"}
~~~

O filtro CORS pode retornar erro sem JSON. Ainda não há PUT nem exclusão de
empresas/vagas expostos na API. A validação de candidato/empresa não é tão
completa quanto a do recrutador; falhas de persistência podem retornar 500.
É uma aplicação de estudo, sem paginação ou moderação automática do texto
profissional informado por usuários. Use dados fictícios, inclusive em descrições.

Veja também [CORS](cors.md), [integração](integracao-frontend-backend.md) e
[spec do recrutador](specs/recrutador/spec.md).
