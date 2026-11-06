# Linketinder REST API

Versão do backend do Linketinder adaptada para funcionar como uma API REST. O projeto foi desenvolvido em Groovy, sem Spring ou outro framework web, utilizando o `HttpServer` nativo do Java.

Esta implementação faz parte do desafio **K2-T4: REST/RESTful API**.

## O que foi implementado

A API possui os três endpoints obrigatórios do desafio:

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| `GET` | `/api/candidatos` | Lista os candidatos | `200 OK` |
| `POST` | `/api/candidatos` | Cadastra um candidato | `201 Created` |
| `POST` | `/api/empresas` | Cadastra uma empresa | `201 Created` |
| `POST` | `/api/vagas` | Cadastra uma vaga | `201 Created` |

Os cadastros recebem dados em JSON e utilizam os DAOs existentes para persistir no PostgreSQL. A listagem consulta os candidatos no banco e devolve um array JSON.

## Como a API foi organizada

Mantive cada parte com uma responsabilidade:

```text
Requisição HTTP
    ↓
Router
    ↓
Handler
    ↓
Controller
    ↓
DAO
    ↓
PostgreSQL
```

- `ApiApp`: monta as dependências e inicia a aplicação;
- `HttpServerConfig`: configura endereço, porta e pool de threads;
- `Router`: relaciona cada endpoint ao seu handler;
- `CandidateHandler`, `CompanyHandler` e `VagaHandler`: leem o método e o JSON da requisição e constroem a resposta HTTP;
- controllers: fazem a comunicação entre os handlers e os DAOs;
- DAOs: executam o SQL e controlam as transações com `commit` e `rollback`.

Essa separação evita que o código responsável pelo HTTP conheça diretamente os detalhes do banco.

## Recursos utilizados

- Java 21;
- Groovy;
- Gradle;
- `com.sun.net.httpserver.HttpServer`;
- `JsonSlurper` para converter JSON em `Map`;
- JDBC e PostgreSQL;
- Spock para os testes;
- HTTP Client do IntelliJ por meio do arquivo `requests.http`.

## Configuração do PostgreSQL

Crie o arquivo `app/src/main/resources/db.properties`:

```properties
db.type=postgres
db.url=jdbc:postgresql://localhost:5432/linketinder
db.user=seu_usuario
db.password=sua_senha
```

O banco deve possuir as tabelas definidas em `sql/Linketinder.sql`. A extensão `pgcrypto` é utilizada para gerar o hash das senhas.

## Como executar

```bash
./gradlew run
# Compila o projeto e inicia a API.
```

Por padrão, a aplicação fica disponível em:

```text
http://localhost:8080
```

Também é possível alterar o endereço e a porta pelas variáveis de ambiente `HOST` e `PORT`.

## Como testar os endpoints

O arquivo `requests.http` possui exemplos completos dos três cadastros. No IntelliJ, abra o arquivo e clique no botão verde ao lado da requisição desejada.

Cada clique em uma requisição `POST` tenta inserir um novo registro no banco. Campos únicos, como CPF e CNPJ, não podem ser repetidos.

## Testes automatizados

Os handlers foram desenvolvidos com TDD. Primeiro foi escrito um teste que falhava; depois foi implementado somente o comportamento necessário para fazê-lo passar.

```bash
./gradlew check
# Executa os testes unitários e de integração e mostra o resumo dos resultados.
```

Os testes dos handlers verificam o recebimento do JSON, a chamada do controller, o status `201` e a resposta `405` para métodos que não são permitidos.

## Integração com o frontend

O [frontend TypeScript/Vite](https://github.com/Athus27/Linketinder-FrontEnd) permanece em um repositório separado. A comunicação utiliza HTTP e JSON; o frontend não acessa diretamente o PostgreSQL.

### Consulta de candidatos

```bash
curl -i http://localhost:8080/api/candidatos
# Consulta a API e mostra o status, os cabeçalhos e o JSON, sem inserir registros.
```

O GET retorna `200` com um array de candidatos, ou `[]` quando não há registros. Cada candidato contém `id`, `nome`, `sobrenome`, `dataNascimento`, `cpf`, `descricao`, `email`, `formacao`, `pais`, `cep` e `competencias`. A senha não é retornada. A data da listagem usa `YYYY-MM-DD`; o cadastro atual espera `dd/MM/yyyy`, conforme `requests.http`.

O handler retorna `500` quando a chamada de listagem ao controller lança uma exceção. Limitação atual: o DAO captura falhas de consulta e pode devolver uma lista vazia, sem distingui-las de ausência de registros.

### Estado da integração e CORS

O frontend já possui service e controller para consultar a lista e converter a resposta para seus modelos. A visualização e a listagem do gerenciamento de candidatos estão conectadas a esse fluxo. Edição e exclusão remotas ainda não foram integradas; os formulários de cadastro continuam usando armazenamento local.

CORS (Cross-Origin Resource Sharing) é aplicado por um `CorsFilter` nos três endpoints, antes dos handlers. Por padrão, a origem autorizada é `http://localhost:5173`. A API devolve `Access-Control-Allow-Origin` para essa origem e `Vary: Origin`, inclusive nas respostas de erro dos handlers. Origens diferentes recebem `403`; chamadas sem `Origin`, como curl e clientes de servidor, continuam permitidas.

O preflight `OPTIONS` retorna `204`, sem executar o handler, para métodos e cabeçalhos autorizados: `GET` e `POST` em candidatos, `POST` em empresas e vagas, e `Content-Type`. Preflights incompatíveis recebem `403`. CORS não implementa autenticação nem sessão.

Para usar outro endereço de frontend:

```bash
FRONTEND_ORIGIN=http://localhost:5174 ./gradlew run
# Inicia a API autorizando exatamente a origem informada, sem barra final ou caminho.
```

Reinicie o backend após alterar essa configuração ou atualizar o código. `localhost` e `127.0.0.1`, assim como portas diferentes, são origens diferentes.

```bash
curl -i -X OPTIONS http://localhost:8080/api/candidatos -H 'Origin: http://localhost:5173' -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: Content-Type'
# Verifica o preflight de POST JSON sem inserir dados no banco.
```

Cadastro pelo formulário e login ainda não estão integrados. Não há autenticação ou sessão implementada nesta API.

### Testes da listagem

Os testes unitários de `CandidateHandlerTest` simulam o controller e verificam lista preenchida, lista vazia, data em formato ISO, falha do controller e métodos não permitidos. Eles não consultam o banco nem validam CORS no navegador.

Os testes de `CorsFilterTest` verificam origem permitida, origem rejeitada, chamadas sem Origin, cabeçalhos em respostas de erro e preflights válidos e inválidos.
