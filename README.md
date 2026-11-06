# Linketinder REST API

Backend do Linketinder em Groovy, sem framework web, usando HttpServer do JDK, JDBC e PostgreSQL. Implementa os cadastros do desafio K2-T4 e consultas para integração com o [frontend](https://github.com/Athus27/Linketinder-FrontEnd).

## Endpoints atuais

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/candidatos` | Lista candidatos |
| GET | `/api/candidatos/{id}` | Busca candidato |
| GET | `/api/empresas` | Lista empresas |
| GET | `/api/vagas` | Lista vagas |
| POST | `/api/candidatos` | Cadastra candidato e competências |
| POST | `/api/empresas` | Cadastra empresa |
| POST | `/api/vagas` | Cadastra vaga e competências |

Consultas retornam 200; cadastros concluídos retornam 201. Listagens devolvem arrays JSON; a consulta por ID devolve um objeto ou 404.

## Configuração e execução

Tecnologias: Java 21, Groovy, Gradle Wrapper, PostgreSQL, JDBC e Spock. A JVM que inicia o Gradle precisa ser compatível com o wrapper; a toolchain não altera o Java do terminal.

Prepare o banco com `sql/Linketinder.sql` e a extensão `pgcrypto`. Crie `app/src/main/resources/db.properties`, ignorado pelo Git:

```properties
db.type=postgres
db.url=jdbc:postgresql://localhost:5432/linketinder
db.user=seu_usuario
db.password=sua_senha
```

```bash
./gradlew run
# Inicia a API em http://localhost:8080.
```

As variáveis `HOST`, `PORT` e `FRONTEND_ORIGIN` alteram endereço, porta e origem autorizada. O padrão do frontend é `http://localhost:5173`.

## Testes automatizados

```bash
./gradlew check
# Executa testes unitários e de integração configurados no projeto.

./gradlew check --rerun-tasks
# Repete as verificações mesmo quando estão UP-TO-DATE.
```

O resumo mostra aprovados, falhos e ignorados por suíte. Testes unitários simulam dependências; os de integração verificam conexão e tabelas do PostgreSQL, não cadastro completo via HTTP.

O `requests.http` contém consultas e cadastros para o IntelliJ. Cada POST tenta gravar novamente os dados; campos únicos não podem ser repetidos indiscriminadamente.

## Organização e limites

O Router registra contextos; o CorsFilter verifica a origem; handlers tratam HTTP/JSON; controllers delegam aos DAOs, que executam SQL. Os handlers registram método, caminho, status e duração, sem corpo. Erros JDBC ainda podem imprimir detalhes do banco no console.

Ainda não há login, sessão, autorização, PUT ou DELETE. A integração frontend/backend é parcial. Duplicidade e outras falhas de persistência ainda podem retornar 500, sem tratamento específico de conflito.

- [Contrato e exemplos da API](docs/api.md)
- [Arquitetura e construção sem framework](docs/arquitetura.md)
- [CORS e integração](docs/cors.md)
