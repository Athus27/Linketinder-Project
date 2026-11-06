# API

Base URL:

```text
http://localhost:8080
```

## Visão geral

A API utiliza JSON nas requisições e respostas.

| Método | Endpoint          | Descrição          |
|--------|-------------------|--------------------|
| `GET`  | `/api/candidatos` | Lista candidatos   |
| `GET`  | `/api/candidatos/{id}` | Busca candidato |
| `DELETE` | `/api/candidatos/{id}` | Exclui candidato |
| `POST` | `/api/candidatos` | Cadastra candidato |
| `GET`  | `/api/empresas`   | Lista empresas     |
| `POST` | `/api/empresas`   | Cadastra empresa   |
| `GET`  | `/api/vagas`      | Lista vagas        |
| `POST` | `/api/vagas`      | Cadastra vaga      |
| `POST` | `/api/auth/login` | Valida login       |

As listagens retornam `200 OK` com um array JSON. Quando não há registros, o retorno é `[]`.

## Candidatos

### Listar candidatos

```http
GET /api/candidatos
```

Resposta:

```json
[
{
"id": 1,
"nome": "Fulano",
"sobrenome": "Silva",
"dataNascimento": "2002-01-15",
"cpf": "111.111.111-11",
"descricao": "Desenvolvedor backend",
"email": "fulano@email.com",
"formacao": "Engenharia da Computação",
"pais": "Brasil",
"cep": "01010-000",
"competencias": ["Java", "Groovy"]
}
]
```

A senha nunca é incluída na resposta.

### Buscar por ID

`GET /api/candidatos/1` retorna um objeto com os campos da listagem, sem senha. A data de resposta usa `yyyy-MM-dd`. ID não numérico retorna `400`; candidato inexistente retorna `404`. POST na rota com ID retorna `405`.

### Excluir por ID

`DELETE /api/candidatos/1` exclui o candidato e seus vínculos tratados pelo DAO. Sucesso retorna `204 No Content`; ID inexistente retorna `404`; ID inválido retorna `400`. Não envie a requisição sem confirmar que o ID pode ser removido.

O endpoint ainda não exige autenticação. Como o DAO converte falhas de busca por ID em `null`, uma indisponibilidade do banco durante a verificação pode ser apresentada como `404`; isso é uma limitação da camada de persistência atual.

### Cadastrar candidato

A data enviada no cadastro usa `dd/MM/yyyy`, diferente do formato das consultas.

```http
POST /api/candidatos
Content-Type: application/json
```

Corpo:

```json
{
"nome": "Fulano",
"sobrenome": "Silva",
"dataNascimento": "15/01/2002",
"email": "fulano@email.com",
"cpf": "111.111.111-11",
"pais": "Brasil",
"cep": "01010-000",
"descricao": "Desenvolvedor backend",
"formacao": "Engenharia da Computação",
"senha": "123456",
"competencias": ["Java", "Groovy"]
}
```

Resposta de sucesso:

```json
{
"mensagem": "Candidato cadastrado com sucesso"
}
```

Senha precisa ter pelo menos 6 caracteres. Se e-mail, CPF ou CNPJ já estiver cadastrado, o endpoint retorna `409 Conflict` com mensagem genérica, sem revelar qual dado coincidiu.

## Empresas

### Listar empresas

```http
GET /api/empresas
```

Resposta:

```json
[
{
"id": 6,
"nome": "Tech Solutions",
"cnpj": "12.345.678/0001-90",
"descricao": "Empresa de tecnologia",
"email": "contato@techsolutions.com",
"pais": "Brasil",
"cep": "01001-000"
}
]
```

A senha nunca é incluída na resposta.

### Cadastrar empresa

```http
POST /api/empresas
Content-Type: application/json
```

Corpo:

```json
{
"nome": "Tech Solutions",
"email": "contato@techsolutions.com",
"cnpj": "12.345.678/0001-90",
"pais": "Brasil",
"cep": "01001-000",
"descricao": "Empresa de tecnologia",
"senha": "123456"
}
```

Resposta de sucesso:

```json
{
"mensagem": "Empresa cadastrada com sucesso"
}
```

Senha precisa ter pelo menos 6 caracteres. E-mail é único sem diferenciar caixa ou espaços externos; duplicidade retorna `409 Conflict`.

## Login

```http
POST /api/auth/login
Content-Type: application/json
```

Corpo:

```json
{
  "email": "contato@techsolutions.com",
  "senha": "123456"
}
```

Em sucesso, retorna `200 OK` com `{ "id": 6, "nome": "Tech Solutions", "tipo": "company" }`. Para candidato, `tipo` é `candidate`. Credenciais inválidas retornam `401` com mensagem genérica; campos ausentes ou JSON inválido retornam `400`. A resposta não inclui e-mail nem senha/hash. Esse endpoint não cria sessão/token nem autoriza chamadas subsequentes; o tipo retornado serve ao frontend para escolher o menu.

## Vagas

### Listar vagas

```http
GET /api/vagas
```

Resposta:

```json
[
{
"id": 1,
"idEmpresa": 6,
"empresa": "Tech Solutions",
"titulo": "Desenvolvedor Groovy",
"descricao": "Desenvolvimento de APIs",
"localVaga": "São Paulo - SP",
"competencias": ["Groovy", "Java"]
}
]
```

### Cadastrar vaga

```http
POST /api/vagas
Content-Type: application/json
```

Corpo:

```json
{
"idEmpresa": 6,
"titulo": "Desenvolvedor Groovy",
"descricao": "Desenvolvimento de APIs",
"localVaga": "São Paulo - SP",
"competencias": ["Groovy", "Java"]
}
```

Resposta de sucesso:

```json
{
  "mensagem": "Vaga cadastrada com sucesso"
}
```

## Status HTTP

| Status                      | Descrição                      |
|-----------------------------|--------------------------------|
| `200 OK`                    | Consulta realizada com sucesso |
| `201 Created`               | Registro criado                |
| `400 Bad Request`           | JSON, senha ou ID inválido      |
| `401 Unauthorized`          | E-mail/senha inválidos          |
| `409 Conflict`              | Dado único já cadastrado        |
| `404 Not Found`             | Rota ou candidato não encontrado |
| `403 Forbidden`             | Origem ou preflight CORS recusado |
| `204 No Content`            | Preflight CORS aceito          |
| `405 Method Not Allowed`    | Método HTTP não permitido      |
| `500 Internal Server Error` | Erro interno do servidor       |

Cadastros concluídos retornam `201`. Ainda não há PUT. JSON válido não garante validação completa de todos os campos; falhas de persistência podem retornar `500`. Alguns DAOs capturam erros de consulta e devolvem lista vazia ou `null`, podendo ocultar uma falha do banco como ausência de dados.
