# Linketinder REST API

Versão do backend do Linketinder adaptada para funcionar como uma API REST. O projeto foi desenvolvido em Groovy, sem Spring ou outro framework web, utilizando o `HttpServer` nativo do Java.

Esta implementação faz parte do desafio **K2-T4: REST/RESTful API**.

## O que foi implementado

A API possui os três endpoints obrigatórios do desafio:

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/candidatos` | Cadastra um candidato | `201 Created` |
| `POST` | `/api/empresas` | Cadastra uma empresa | `201 Created` |
| `POST` | `/api/vagas` | Cadastra uma vaga | `201 Created` |

Os endpoints recebem dados em JSON e utilizam os DAOs que já existiam no Linketinder para persistir as informações no PostgreSQL.

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
./gradlew test
# Executa os testes unitários com banco e requisições simulados.

./gradlew integrationTest
# Executa os testes que dependem da conexão real com o PostgreSQL.
```

Os testes dos handlers verificam o recebimento do JSON, a chamada do controller, o status `201` e a resposta `405` para métodos que não são permitidos.
