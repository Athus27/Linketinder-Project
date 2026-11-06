# Plano executável — recrutador (Issue #3)

**Estado:** plano pós-spec revisada pelo Codex sob delegação; validação do Tutor
pendente. Tarefa 1 foi commitada em `82ea1a2` (`feat: adicionar persistência e
modelo de recrutadores`). Um agente executa somente uma tarefa por vez; Codex
revisa e faz um commit próprio por tarefa e por repositório afetado.
Não haverá reset público, credencial local exposta, PR, merge ou entrega.

## Isolamento e testabilidade

Os testes PostgreSQL usam `ConnectionFactory` já existente por injeção, sem
`DB_SCHEMA` global: `TemporarySchemaConnectionFactory(ConnectionFactory base,
String schema)` valida `schema` com `^[a-z][a-z0-9_]{0,62}$`, abre a conexão
delegada, executa `SET search_path TO "<schema>", public` e implementa
`getConnection()`. O fixture cria schema aleatório, aplica
`sql/Linketinder.sql`, V1 e V2 na mesma conexão (extensão `pgcrypto` fica em
`public`), insere apenas `@example.test`, executa o caso e remove somente esse
schema em `finally`; nunca toca no schema público. A aplicação real continua
usando `db.properties` ignorado, sem imprimir configuração. A concorrência usa
duas conexões do mesmo schema temporário e `CountDownLatch`.

## Tarefas

### 1. Persistência V2 e modelo

**Camada:** SQL, conexão de teste e modelo.
**Camada/arquivos:** SQL: novo `sql/migrations/V2__recruiter_and_like_author.sql`;
`app/src/main/groovy/org/linketinder/model/Pessoa/Recruiter.groovy` e ajuste
`app/src/main/groovy/org/linketinder/model/Pessoa/User.groovy`; testes novos
`app/src/integrationTest/groovy/org/linketinder/dao/RecruiterMigrationTest.groovy`,
`app/src/integrationTest/groovy/org/linketinder/dao/TemporarySchemaConnectionFactory.groovy`,
`app/src/integrationTest/groovy/org/linketinder/dao/SyntheticDatabaseFixture.groovy`;
`sql/DadosIniciais.sql` permanece inalterado; fixtures novas serão compatíveis
com seus dados legados.
**Contrato:** `Recruiter(idRecrutador,idEmpresa,area,descricao,cpf,dataNascimento,ativo)`;
V2 aditiva torna país/CEP de `User` anuláveis, cria 1:1 User–Company recruiter,
CPF único/normalizado, `ativo`, FK e `Curtida.id_recrutador_autor` preenchido
somente na primeira autoria; unicidade do par e `data_match` único permanecem.
**RED/GREEN:** o teste inicial com schema base+V1 falhou por ausência de
`Recruiter`; após aplicar V2, o teste focado passou provando constraints,
legados preservados, normalização/unicidade de CPF, default/desativação,
nulabilidade nova, hash sintético e FK de autoria. O fixture também abre duas
conexões no mesmo schema para T3, mas não testa comportamento concorrente nem
primeira autoria atômica nesta tarefa; a migration não promete proteger autoria
de SQL arbitrário. Comandos executados:
`./gradlew integrationTest --tests '*RecruiterMigrationTest'`.
**Critérios/checklist (aceites 2, 11, 18):** [x] anonimato não regredido
(nenhuma projeção HTTP/tela alterada); [x] autenticação/autorização não é
deslocada para o banco; [x] proteção de segredos e dados sintéticos; [x] V2
aditiva e schema temporário limpo; [x] RED/GREEN e limitações registradas;
[x] plano/CHANGELOG/PROMPTS atualizados. A concorrência e a primeira autoria
ficam explicitamente para T3. Commit backend permanece para o Codex.

### 2. Sessão e gestão do recrutador

**Camada:** autenticação, autorização HTTP, controller, DAO e testes Spock.
**Dependência:** T1. **Arquivos:** novos
`app/src/main/groovy/org/linketinder/auth/Session.groovy`,
`SessionStore.groovy`, `AuthPrincipal.groovy`,
`app/src/main/groovy/org/linketinder/controller/RecruiterController.groovy`,
`app/src/main/groovy/org/linketinder/dao/user/RecruiterDAO.groovy`,
`app/src/main/groovy/org/linketinder/http/handler/RecruiterHandler.groovy`;
ajustes `AuthDAO.groovy`, `AuthController.groovy`, `AuthHandler.groovy`,
`Router.groovy`; unitários novos
`app/src/test/groovy/org/linketinder/auth/SessionStoreTest.groovy`,
`app/src/test/groovy/org/linketinder/dao/RecruiterDAOTest.groovy`,
`app/src/test/groovy/org/linketinder/http/handler/RecruiterHandlerTest.groovy`.
**Contrato:** Bearer resolvido por requisição para `SessionStore` thread-safe,
token opaco, TTL 4h, logout idempotente e reinício invalidador; login retorna
`token,id,nome,tipo,idEmpresa` mínimo. CRUD de `/api/recrutadores` deriva
empresa da sessão, valida campos e nunca serializa CPF/nascimento/hash.
**RED/GREEN:** o teste inicial de `SessionStoreTest` falhou por classes
ausentes; depois passaram 2 testes de TTL/revogação com `Clock` injetável.
Passaram 130 testes unitários e 11 testes de integração
com fixture de schema temporário. A validação cobre tokens distintos,
expiração em quatro horas, logout idempotente, 401/403/404/409, validações de
CPF/data/senha, allowlist sem PII, criação atômica, isolamento por empresa,
desativação e invalidação de login/sessão existente. A verificação final foi
`./gradlew check --rerun-tasks --console=plain` (130 unitários e 11 de
integração); nenhum banco público foi
alterado. Na revisão Codex, o RED adicional cobriu o bypass de sessão nula,
falhas de autenticação/listagem fora do boundary, IDs zero/overflow e
`idEmpresa` não inteiro; o GREEN passou após remover o modo sem sessão,
centralizar o erro 500 genérico e endurecer a validação. Também passaram
casos de senha UTF-8 em 72/74 bytes e limites de campos, além de rollback
completo em CPF duplicado e conflito de e-mail normalizado.
**Checklist (aceites 1–7, 15, 17):** [x] autorização por requisição e sem
HttpContext compartilhado; [x] anonimato, allowlist e segredos; [x] proteção de
atores e dados sintéticos; [x] RED/GREEN e regressão/limitações reais;
[x] atualizar plano/CHANGELOG/PROMPTS; [ ] commit backend pelo Codex.

Limitação para T3: `ApiApp.buildRouter` injeta a fábrica apenas em
`AuthDAO`/`RecruiterDAO`; os DAOs legados ainda precisam receber a fábrica para
testes HTTP isolados. Como país/CEP de `User` são nullable após V2, T3 deve
manter a obrigatoriedade de candidato/empresa na validação HTTP, com testes de
cadastros incompletos.

### 3. Anonimato, curtidas, matches e autorizações backend

**Camada:** handlers, controllers, DAOs, projeções, HTTP e PostgreSQL.
**Dependência:** T2. **Arquivos:** ajustes `Router.groovy`, handlers,
`CandidatoController.groovy`, `CompanyController.groovy`, `VagaController.groovy`,
`CurtidaController.groovy`, DAOs `CandidateDAO.groovy`, `CompanyDAO.groovy`,
`VagaDAO.groovy`, `CurtidaDAO.groovy`; novo
`app/src/main/groovy/org/linketinder/http/handler/CurtidaHandler.groovy`;
projeções novas `app/src/main/groovy/org/linketinder/model/CandidatePublicView.groovy`,
`CompanyPublicView.groovy`, `MatchView.groovy`; testes novos
`app/src/test/groovy/org/linketinder/http/handler/PrivacyProjectionTest.groovy`,
`CurtidaHandlerTest.groovy`, `VagaAuthorizationTest.groovy`; integração nova
`app/src/integrationTest/groovy/org/linketinder/http/RecruiterFlowTest.groovy`.
**Contrato:** allowlists antes/depois do match; `POST /api/curtidas`,
`GET /api/matches`, `GET /api/vagas?minhas=true`, POST de vaga e DELETE do
candidato usam Bearer, ator derivado, vínculo empresa-vaga-candidato,
idempotência transacional e 404 para recurso alheio.
**RED/GREEN:** ajustar testes que afirmavam identidade insegura sem remover
cobertura; provar duas conexões/recrutadores, empresa terceira, concorrência,
contato somente pós-match, logout e soft-delete com `./gradlew check`.
**Checklist (aceites 8–14, 16–18):** [ ] PII ausente em JSON/erros/logs;
[ ] autenticação, autorização por papel e vínculo; [ ] proteção de atores,
primeira autoria/timestamp atômicos e fixtures sintéticas; [ ] unitário,
HTTP/Postgres, regressão e limitações reais; [ ] atualizar
plano/CHANGELOG/PROMPTS; commit.

### 4. Frontend: sessão, services e contratos

**Camada:** estado de sessão, services, tipos, controllers e testes Vitest.
**Dependência:** T2/T3. **Repositório:** `Linketinder-FrontEnd`. **Arquivos:**
`src/main.ts`, `src/types/AppState.ts`, `src/types/UserType.ts`,
`src/types/LoginResponse.ts`, novos `src/types/SessionState.ts`,
`src/types/RecruiterResponse.ts`, `src/types/LikeRequest.ts`,
`src/types/MatchResponse.ts`, `src/types/JobRequest.ts`;
`src/services/LoginService.ts`, `CandidateService.ts`, `CompanyService.ts`,
`JobService.ts`, novos `src/services/SessionService.ts`,
`RecruiterService.ts`, `LikeService.ts`, `MatchService.ts`; ajustes nos
controllers existentes e testes explícitos em `tests/services/` e
`tests/controllers/`.
**Contrato:** token apenas em memória, logout/revogação e Bearer por request;
tipos anonimizados e recruiter/like/match/job POST; nenhum ID de identidade em
localStorage. **RED/GREEN:** Vitest dos status/headers/DTOs e build:
`npm run test:run && npm run build`.
**Checklist (aceites 14–18):** [ ] sem token/PII persistido e com allowlist;
[ ] autenticação/autorização continua no servidor; [ ] atores, segredos e
fixtures sintéticas protegidos; [ ] Vitest, build, regressão e limitações reais;
[ ] registrar resultado neste plano backend, CHANGELOG/PROMPTS; commit frontend
e depois revisão Codex.

### 5. Frontend: telas reais protegidas

**Camada:** páginas, navegação, cards, formulários e testes DOM.
**Dependência:** T4. **Arquivos:** `src/view/components/layout/Header.ts`,
`src/view/pages/LoginPage.ts`, novos `RecruiterManagementPage.ts`,
`RecruiterJobsPage.ts`, `MatchesPage.ts`; ajustes `JobRegisterPage.ts`,
`JobFeedPage.ts`, `CandidateFeedPage.ts`, `CandidateLikePage.ts`,
`CompanyLikePage.ts`, `src/view/components/cards/CandidateProfileCard.ts`,
`CompanyProfileCard.ts`, `JobProfileCard.ts`, `src/view/components/forms/JobForm.ts`,
`src/renderAll.ts` e `src/app/LinketinderApp.ts`; testes DOM novos
`tests/view/RecruiterManagementPage.test.ts`, `RecruiterPrivacy.test.ts`;
adicionar `jsdom` como devDependency somente para os testes DOM que o ambiente
realmente exigir.
**Contrato:** menus vêm do papel/sessão, gestão e vagas próprias protegidas,
cadastro via API, cards anonimizados e contato só no match; demos continuam
isoladas e `main` não inicializa demo com PII no localStorage real.
**RED/GREEN:** testes DOM de ator errado, loading/erro/vazio, PII e logout;
`npm run test:run && npm run build`.
**Checklist (aceites 13–16, 18):** [ ] navegação não seleciona terceiros;
[ ] privacidade antes/depois e nenhum token/PII; [ ] autorização não depende
da tela; [ ] fixtures sintéticas, regressão/build e limitações reais;
[ ] registrar no plano backend/CHANGELOG/PROMPTS; commit frontend.

### 6. E2E e documentação

**Camada:** integração HTTP/DB/DOM, documentação e aceite.
**Dependência:** T1–T5. **Arquivos backend:** `README.md`, novo
`docs/specs/recrutador/PR.md`, `requests.http`, novos
`app/src/integrationTest/groovy/org/linketinder/e2e/RecruiterAcceptanceTest.groovy`,
`scripts/migrations/recruiter-v2-test.sh`,
`scripts/tests/recruiter-e2e.sh`. **Contrato:** schema temporário com duas empresas, dois
recrutadores da primeira, terceiro da outra e candidato; tokens são placeholders.
**RED/GREEN:** executar HTTP/DB/DOM reais para autorização, PII, concorrência,
soft-delete, logout/expiração e legado; `./gradlew check`, `npm run test:run`,
`npm run build`; revisar checklist de aceite 1–18, evidência e pendências
honestas. `PR.md` contém `Closes #3`, sem publicar.
**Checklist (aceites 1–18):** [ ] cada critério marcado com evidência,
anonimato e autorização; [ ] proteção de atores, segredos/PII e sem reset;
[ ] requests/fixtures sintéticos; [ ] regressão, limitações e cenários
concorrentes registrados; [ ] CHANGELOG/PROMPTS finais; commit por repo, sem
PR/merge.

**Conflitos/estado:** nenhum conflito novo encontrado. O baseline informado
(backend 94; frontend 31 + build) e Postgres ligado ainda não foi reexecutado
nesta redação; cada tarefa só muda para concluída após seus testes reais.
