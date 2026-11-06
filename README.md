# Linketinder REST API

Backend do Linketinder em Groovy, sem framework web, usando HttpServer do JDK, JDBC e PostgreSQL. Implementa os cadastros do desafio K2-T4 e consultas para integração com o [frontend](https://github.com/Athus27/Linketinder-FrontEnd).

## Endpoints atuais

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/candidatos` | Lista candidatos |
| GET | `/api/candidatos/{id}` | Busca candidato |
| DELETE | `/api/candidatos/{id}` | Exclui candidato |
| GET | `/api/empresas` | Lista empresas |
| GET | `/api/vagas` | Lista vagas |
| POST | `/api/candidatos` | Cadastra candidato e competências |
| POST | `/api/empresas` | Cadastra empresa |
| POST | `/api/vagas` | Cadastra vaga e competências |
| POST | `/api/auth/login` | Valida e-mail/senha e retorna o perfil |

Consultas retornam 200; cadastros concluídos retornam 201; exclusões concluídas retornam 204 sem corpo. Login válido retorna 200 e inválido, 401. Senhas de cadastro precisam ter ao menos 6 caracteres; dados únicos duplicados retornam 409. Listagens devolvem arrays JSON; consultas e exclusões por ID retornam 404 quando o candidato não existe.

## Configuração e execução

Tecnologias: Java 21, Groovy, Gradle Wrapper, PostgreSQL, JDBC e Spock. A JVM que inicia o Gradle precisa ser compatível com o wrapper; a toolchain não altera o Java do terminal.

Para um banco novo, prepare o schema com `sql/Linketinder.sql` e a extensão `pgcrypto`; o schema cria o índice único de e-mail normalizado. Em bancos existentes, primeiro confirme que não há e-mails equivalentes após `lower(trim(email))`:

```sql
SELECT count(*) FROM (
  SELECT lower(trim(email))
  FROM "User"
  GROUP BY lower(trim(email))
  HAVING count(*) > 1
) AS duplicados;
```

O resultado precisa ser `0`. Depois aplique a migração usando o usuário configurado no PostgreSQL:

```bash
psql -U seu_usuario -d linketinder -f sql/migrations/V1__unique_user_email_normalized.sql
# Cria o índice que impede duplicidade ignorando caixa e espaços externos.
```

Crie `app/src/main/resources/db.properties`, ignorado pelo Git:

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

O `requests.http` contém consultas, cadastros e exclusão para o IntelliJ. Cada POST tenta gravar novamente os dados; campos únicos não podem ser repetidos indiscriminadamente. A requisição DELETE é destrutiva: confira o ID antes de executá-la.

## Organização e limites

O Router registra contextos; o CorsFilter verifica a origem; handlers tratam HTTP/JSON; controllers delegam aos DAOs, que executam SQL. Os handlers registram método, caminho, status e duração, sem corpo. Erros JDBC ainda podem imprimir detalhes do banco no console.

O login verifica a senha com `pgcrypto` e retorna somente ID, nome e tipo de perfil. Ele não cria sessão/token nem protege os demais endpoints; a seleção de menus no frontend é apenas uma regra de interface. A exclusão de candidato também não exige autenticação, então não exponha a API fora do ambiente local de estudo. PUT e exclusão de empresas/vagas ainda não estão implementados.

- [Contrato e exemplos da API](docs/api.md)
- [Como frontend e backend se conectam](docs/integracao-frontend-backend.md)
- [Arquitetura e construção sem framework](docs/arquitetura.md)
- [CORS e integração](docs/cors.md)
