# PROMPTS — K2-T5 IA essentials / Recrutador

## Autoria e papéis

- **Copilot CLI:** autoria dos artefatos desta etapa e, futuramente, do código
  solicitado; deve trabalhar sobre a implementação real e não inventar
  garantias.
- **Codex:** orquestrador e revisor.
- **Usuário:** delegou ao Copilot as decisões rotineiras de levantamento,
  redação e validação dos artefatos.
- **Tutor:** ainda não aprovou as decisões de negócio que deverão constar da
  spec. Nenhuma spec ou plano de feature foi escrito nesta etapa.

## Registro da sessão — etapa 1

ID da sessão Copilot: `16beded6-8dbc-4461-a02f-534d3027c8df`.

Resumo fiel do pedido: preparar contexto, skill, changelogs, prompts e
levantamento factual do desafio de recrutador em backend e frontend; explorar
entidades, relações, match, camadas, testes e execução; registrar riscos reais
de anonimato e autenticação; não implementar feature/spec/plano, não alterar
banco e não fazer operações GitHub ou commits.

Arquivos criados nesta sessão:

- `AGENTS.md` e `CHANGELOG.md` na raiz de cada repositório;
- `skills/feature-com-spec/SKILL.md`;
- `PROMPTS.md`;
- `docs/specs/recrutador/levantamento.md`.

Verificações realmente executadas: inspeção do status limpo e da branch
`codex/preparacao-recrutador` nos dois repositórios; leitura seletiva de
README, manifests, fontes de modelos/DAOs/controllers/handlers, telas,
services, app local, testes e documentação; não foram executados Gradle,
Vitest, build, servidor ou banco nesta etapa. Não foram lidos `.env`,
`db.properties`, credenciais ou dados pessoais.

Não registrar neste arquivo tokens, senhas, credenciais, dados pessoais ou
resultados não observados.

## Registro da revisão da etapa 1

Nesta revisão foram corrigidas apenas as referências de `plano.md`, a redação
sobre revisão técnica pelo Codex sob delegação de Athus e decisões de negócio
pendentes de validação humana/Tutor, as orientações de schema/migrações/banco
de testes no `AGENTS.md` do backend e o registro desta sessão. Não foram
alterados código, spec, plano ou banco, nem feitos commits.

## Respostas propostas às perguntas obrigatórias — etapa 2

Estas respostas são decisões propostas pelo Codex sob delegação de Athus para
redação da spec. O Tutor ainda não as aprovou; não atribuir essa aprovação ao
Tutor nem ao usuário. A revisão técnica do Codex e o commit da spec devem
ocorrer antes de qualquer código.

1. **Atores e ciclo de vida:** a empresa autenticada cria, lista e desativa
   logicamente seus recrutadores. Cada recrutador pertence a uma única empresa,
   não administra vagas nem recrutadores e não pode fazer login ou usar sessão
   após desativação. Candidato, empresa, vaga e recrutador só podem ser
   alterados/removidos pelo titular ou papel explicitamente autorizado; nesta
   feature o DELETE de candidato é apenas do próprio titular, a empresa mantém
   suas vagas e somente a empresa dona gerencia seus recrutadores.
2. **Empresa e vagas:** uma empresa pode ter várias vagas. A empresa cria suas
   vagas; recrutadores apenas analisam todas as vagas da própria empresa.
   Qualquer operação deriva empresa e ator da sessão e valida o vínculo no
   servidor.
3. **Voto:** a curtida é por par candidato-vaga e representa a empresa. Não há
   revogação nesta feature. Repetições são idempotentes, não criam novo match e
   preservam o primeiro recrutador autor; unicidade e concorrência são
   garantidas no banco.
4. **Dois recrutadores:** são usuários distintos da mesma empresa. Eles não
   geram votos duplicados nem matches distintos; o primeiro autor é apenas
   auditoria operacional.
5. **Consulta:** candidato consulta oportunidades anonimizadas; empresa e seus
   recrutadores consultam vagas próprias e candidatos anonimizados. A empresa
   consulta dados de gestão de seus colaboradores sem CPF/nascimento. Match
   libera somente os contatos previstos para o par e ator correspondente.
6. **Anonimato:** antes do match, somente pseudônimo/ID técnico não
   identificável, competências, formação e experiência permitidas; não há
   nome, e-mail, CPF, CNPJ, nascimento, idade, CEP ou descrição livre
   potencialmente identificável. Depois do match, a empresa correspondente e
   seus recrutadores podem ver nome, sobrenome, e-mail e descrição profissional
   do candidato; o candidato pode ver nome e e-mail da empresa. Nunca são
   expostos CPF, CNPJ, nascimento, senha ou hash. O candidato não recebe dados
   pessoais do recrutador.
7. **Validações:** e-mail global é `lower(trim)`, válido e único; CPF de
   recrutador tem 11 dígitos, rejeita repetição e dígitos verificadores
   inválidos; nascimento é ISO real, idade mínima 18 e sem futuro; senha tem
   8–72 bytes UTF-8 e ao menos letra e número. Nome, área e descrição são
   obrigatórios e têm limites explícitos na spec. País e CEP da `User` podem
   ser nulos para recrutadores, sem retrovalidar ou destruir legados.
8. **IDs, sessão e segurança:** login cria token opaco seguro, somente em
   memória no frontend, enviado como Bearer; expira em 4 horas, logout revoga
   e reinício da API invalida sessões. O servidor deriva ID, tipo e empresa da
   sessão; sem sessão é 401 e papel/empresa inadequados são 403. IDs enviados
   pelo cliente não escolhem o ator, candidato ou empresa.
9. **Mensagens/status:** entradas inválidas retornam 400 com erro estruturado
   sem segredos; ausência de autenticação retorna 401; papel, vínculo ou
   recurso alheio retorna 403/404 conforme a política do endpoint; conflito
   de unicidade retorna 409; sessão expirada/inativa exige novo login. O
   detalhe de CPF, senha, hash e dados pessoais nunca aparece em erros/logs.
10. **Compatibilidade e dados:** GETs públicos mantidos usam DTOs/projeções
    allowlist anonimizados. Curtidas, matches e criação de vagas passam a ter
    API como fonte de verdade; demos isoladas de `LinketinderApp` permanecem
    apenas demonstrativas e não autorizam operações. A migração futura é V2
    aditiva, com testes em database/schema temporário e fixtures
    `@example.test`.

## Registro da sessão — plano da feature

Sessão atual: leitura manual de `skills/feature-com-spec/SKILL.md`, da spec
revisada e dos dois repositórios na branch `codex/recrutador`. Baseline recebido:
backend 94, frontend 31 + build; Postgres ligado. Foram apenas inspecionados
arquivos e escritos `docs/specs/recrutador/plano.md`, este registro e o
`CHANGELOG.md`; não foram executados testes, não foram lidos segredos e não
houve alteração em código, migração, banco, commit ou push.

Decisão de infraestrutura: menor mudança é manter `ConnectionFactory` como
seam injetável e criar no escopo de integração uma fábrica de schema temporário
com assinatura concreta `TemporarySchemaConnectionFactory(ConnectionFactory
base, String schema)`, `getConnection()` configurando `search_path` para
`schema, public`; fixture aplica schema inicial+V1+V2 e remove somente seu
schema. Não será introduzido `DB_SCHEMA` global nem atributo compartilhado de
identidade HTTP.

Conflito novo: nenhum encontrado. A validação Tutor permanece pendente; nomes
finais de DTOs e telas só podem variar mantendo os contratos da spec. O plano
exige commits separados por tarefa/repositório e atualização posterior deste
registro com resultados observados, sem declarar tarefa concluída antes dos
testes reais.

## Referências da feature

- Issue: https://github.com/Athus27/Linketinder-Project/issues/3
- Sessão da issue: `8d5762a3-ba6d-4a38-81bf-e043305f0332`.
- Preparação já commitada: backend `d1ab06e` e frontend `3fbb374`.
- Após estes ajustes, a spec está revisada tecnicamente pelo Codex sob
  delegação explícita; não está aprovada pelo Tutor. A validação de negócio
  com Athus/Tutor permanece pendente antes da entrega.

## Registro da T2 — sessão e gestão do recrutador

Implementação realizada pelo Copilot sem commit, push, PR, merge ou alteração
do schema público. A T1 existente foi confirmada no histórico como commit
`82ea1a2` (`feat: adicionar persistência e modelo de recrutadores`).

Foram adicionados `SessionStore`, `Session`, `AuthPrincipal`, validação
server-side do principal, login/logout Bearer, `RecruiterDAO`,
`RecruiterController`, `RecruiterHandler`, fábrica testável da `ApiApp`,
injeção opcional de `ConnectionFactory` em `AuthDAO`/`RecruiterDAO` e CORS para
`Authorization`. O cadastro usa uma transação única User+Recruiter, deriva a
empresa da sessão, trata SQLState `23505` como conflito genérico e nunca
serializa CPF, nascimento, senha ou hash. Recrutadores inativos não autenticam
nem usam sessões emitidas anteriormente.

RED observado: `SessionStoreTest` não compilou antes das classes de sessão
existirem. GREEN observado: 107 testes unitários passaram inicialmente, e o fluxo
`RecruiterFlowTest` passou em fixture PostgreSQL temporária, e a verificação
final `./gradlew check --rerun-tasks --console=plain` passou. Não foram
executados frontend, T3, migrações no schema público ou operações GitHub.

Revisão Codex pré-commit: o modo inseguro sem `SessionStore` foi removido e o
construtor rejeita `null`; login sempre emite token e allowlist. O boundary do
`RecruiterHandler` agora converte indisponibilidade de auth/listagem em
`{erro,codigo:INTERNAL_ERROR}` genérico. IDs não positivos, overflow e
`idEmpresa` não inteiro são rejeitados sem escolher vínculo. Foram adicionados
testes de limites de campos e senha UTF-8 (72/74 bytes), rollback completo em
CPF duplicado, conflito de e-mail normalizado sem valores na mensagem e RED →
GREEN da revisão. A execução final da revisão foi
`./gradlew check --rerun-tasks --console=plain`; frontend, T3 e schema público
continuam fora do escopo.

Resultado final após a revisão: `./gradlew check --rerun-tasks --console=plain`
passou com 130 testes unitários e 11 testes de integração.

## Registro da T3 — anonimato, curtidas, matches e autorizações

Execução interrompida anteriormente pelo Codex durante revisão porque havia
tentativa de preservar bypasses de segurança para testes legados. O Copilot
corrigiu a implementação sem commit, push, PR ou merge: removidos todos os
caminhos de compatibilidade sem sessão nos handlers de candidato, empresa e
vaga; testes legados foram ajustados para allowlist/autenticação; nenhum
frontend ou schema público foi alterado.

Foram corrigidas a injeção da fábrica em `CandidateDAO.buscarPorId` e
competências, a consulta nullable de empresas, a projeção pré-match e o bug
de `deuMatch` quando a empresa curte primeiro. Router e handlers agora
expõem curtidas/matches e vagas próprias com autorização derivada da sessão.

Verificações finais: `./gradlew check --rerun-tasks --console=plain`, com 121
testes unitários e 14 de integração aprovados. A integração inclui
`ApiHttpProjectionTest` com `HttpServer`/porta 0 e
`CurtidaConcurrencyTest` com duas conexões, `CountDownLatch`,
`ExecutorService`, repetição, match nos dois sentidos e primeiro autor
estável. Tarefas 4–6 continuam incompletas; frontend e aceite E2E completo
não fazem parte desta execução.

Revisão final da T3: o teste concorrente usa `Callable<Map>`, exige sucesso
nas duas respostas, exatamente um primeiro voto e um replay, e compara
autor/timestamp antes e depois do replay. Também cobre CPFs sintéticos válidos
e os sentidos empresa-primeiro/candidato-segundo e candidato-primeiro/empresa-
segundo. Logs de Candidate/Company/Vaga e competências não incluem mensagens
SQL; teste unitário captura stdout numa falha com e-mail, CPF e hash
fictícios. O teste HTTP parseia JSON e exige keysets exatos e uma linha por
projeção no schema isolado. A regressão HTTP T3 cobre apenas GETs públicos e
Authorization malformado; autenticação completa, curtidas privadas e contatos
ficam para T6.

## Registro da T1 — persistência V2 e modelo

Sessão Copilot: `167e783c-7030-4cbe-b90b-3f2631e1a8d3`. Branch:
`codex/recrutador`. O RED foi executado antes da implementação com schema
temporário contendo apenas base+V1; `RecruiterMigrationTest` falhou porque a
tabela `Recruiter` não existia. O GREEN foi executado após a V2, modelo
`Recruiter` e fixtures: 6 testes passaram, cobrindo relação User 1:1 e
Company 1:N, FK, CPF normalizado/único, `ativo`, desativação, nulabilidade
nova, legado preservado, hash sintético e autoria nullable/validada.

Também foi verificado que duas conexões independentes apontam para o mesmo
schema temporário, sem executar migração no schema público. Concorrência,
primeira autoria e atomicidade continuam não implementadas e foram deixadas
para T3, conforme o ajuste solicitado. Nenhuma credencial foi exibida; não
houve commit, push, merge, PR ou leitura de fontes frontend.

## Registro da T4 — frontend: sessão, services e contratos

Implementação realizada no repositório `Linketinder-FrontEnd`, branch
`codex/recrutador`, sem commit, push, PR, merge ou alteração no backend.
Foram adicionados estado/sessão em memória, helper de erros HTTP, autenticação
Bearer por requisição, logout com limpeza garantida, tratamento de 401 e
services/DTOs de recrutador, curtida, match e vaga. Os services existentes
foram ajustados para as allowlists anônimas e `formation`; nenhum token ou PII
é persistido. `main.ts` e telas não foram modificados, pois pertencem à T5.

Testes adicionados/ajustados cobrem headers, estado antes/depois de login,
logout e erro de rede, 401, papéis e IDs inválidos, payloads sem ator,
recrutador CRUD, vaga/match e anonimato. A integração
`CandidateService.integration.test.ts` exige explicitamente
`VITE_RUN_API_INTEGRATION=true` e não imprime dados. Verificações observadas:
`npm run test:run` com 39 testes aprovados e 1 teste opt-in ignorado; `npm run
build` aprovado. A UI real e a integração externa permanecem para T5/T6.

### Revisão Codex da T4

Execução `7c0e2c96-a813-48c1-a074-2f0d0e8ffda3`: sem commit, push, PR ou
merge. `LikeService` agora valida e devolve o DTO completo de curtida;
`LoginService` rejeita tipos, IDs, papéis e vínculos inválidos; services de
recrutador e match aplicam allowlists explícitas; `MatchService` preserva
`dataMatch: null` e rejeita datas inválidas. Foram adicionados casos de
repetição/match, JSON inválido, tipos inválidos, PII extra e data nula.
`VITE_RUN_API_INTEGRATION=true` permaneceu desabilitado: o teste externo foi
ignorado, não executado. T5 (telas) e T6 (integração externa/E2E) continuam
pendentes.
