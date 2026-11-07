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
| POST | `/api/auth/logout` | Revoga a sessão Bearer |
| GET, POST | `/api/recrutadores` | Empresa lista e cadastra seus recrutadores |
| DELETE | `/api/recrutadores/{id}` | Empresa desativa seu recrutador |
| GET | `/api/vagas?minhas=true` | Empresa/recrutador consulta as vagas da própria empresa |
| POST | `/api/curtidas` | Registra interesse no par candidato–vaga |
| GET | `/api/matches` | Lista matches e contatos autorizados do usuário |

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

Antes de iniciar esta versão, aplique também a V2. Ela cria `Recruiter` e a
autoria da curtida; sem ela, até o login de perfis antigos falha. Faça backup e
confira o banco de destino antes de executar. A V2 não deve ser reaplicada.

```bash
pg_dump -U seu_usuario -d linketinder -Fc -f backup-antes-v2.dump
# Salva um backup local; proteja esse arquivo e não o versione.
psql -U seu_usuario -d linketinder -v ON_ERROR_STOP=1 -1 -f sql/migrations/V2__recruiter_and_like_author.sql
# Aplica a V2 em uma transação, interrompendo se houver erro.
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

O resumo mostra aprovados, falhos e ignorados por suíte. A integração usa schemas temporários e cobre migração, persistência, concorrência de curtidas e projeções HTTP. A validação manual do fluxo completo está em [validação do recrutador](docs/specs/recrutador/validacao.md).

O `requests.http` contém consultas, cadastros e exclusão para o IntelliJ. Cada POST tenta gravar novamente os dados; campos únicos não podem ser repetidos indiscriminadamente. A requisição DELETE é destrutiva: confira o ID antes de executá-la.

## Organização e limites

O Router registra contextos; o CorsFilter verifica a origem; handlers tratam HTTP/JSON; controllers delegam aos DAOs, que executam SQL. Os handlers registram método, caminho, status e duração, sem corpo. Os erros das operações de cadastro não imprimem valores SQL ou credenciais.

O login verifica o hash com `pgcrypto` e emite token opaco, mantido em memória
por até quatro horas. Envie `Authorization: Bearer <token>` nas operações
protegidas. Logout e reinício da API invalidam sessões; recrutador inativo
perde acesso mesmo com token anterior. A exclusão de candidato exige o próprio
titular e a criação de vaga exige empresa autenticada. PUT e exclusão de
empresas/vagas ainda não estão implementados.

Antes do match, candidatos são identificados apenas por ID técnico e mostram
competências/formação. O match libera os contatos do par autorizado, nunca
CPF, CNPJ, nascimento, senha ou hash. O recrutador representa sua empresa na
curtida; repetições não duplicam voto ou match. Consulte a
[spec](docs/specs/recrutador/spec.md) para os contratos completos. Este é um
ambiente local de estudo, não uma implantação de produção.

- [Contrato e exemplos da API](docs/api.md)
- [Como frontend e backend se conectam](docs/integracao-frontend-backend.md)
- [Arquitetura e construção sem framework](docs/arquitetura.md)
- [CORS e integração](docs/cors.md)
