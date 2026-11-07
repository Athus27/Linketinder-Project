# Changelog

## [Não lançado]

### Adicionado

- T3: projeções públicas anonimizadas, autorização obrigatória em operações
  protegidas, rotas de curtida/match e vagas próprias, com teste HTTP real,
  teste PostgreSQL de concorrência e preservação do autor da primeira curtida.
- Revisão final da T3: concorrência agora usa `Callable<Map>` e comprova dois
  sucessos, ordem de primeiro autor, timestamp imutável, os dois sentidos do
  match e replays; logs dos DAOs não serializam mensagens SQL brutas.
- Sessões opacas thread-safe com TTL de quatro horas, logout idempotente,
  autenticação de recrutador/empresa e gestão de recrutadores por empresa,
  com validações, DTOs allowlistados e integração em schema temporário.
- Migração V2 aditiva de recrutadores, autoria opcional de curtida e fixture
  PostgreSQL isolado por schema temporário, com teste RED/GREEN focado.
- Contexto local do backend em `AGENTS.md`, com stack, comandos, domínio e lacunas de anonimato/autorização.
- Skill `skills/feature-com-spec/SKILL.md` para condução de especificação, plano e implementação incremental entre os dois repositórios.
- Registro de autoria e verificações desta preparação em `PROMPTS.md`.
- Levantamento factual inicial do recrutador em `docs/specs/recrutador/levantamento.md`.
- Spec proposta de recrutador em `docs/specs/recrutador/spec.md`, com contratos, anonimato, autorização, modelo de dados e critérios de aceite.
- Plano executável em `docs/specs/recrutador/plano.md`, dividido em seis commits/tarefas e com isolamento PostgreSQL por schema temporário.

### Modificado

- T3: todos os DAOs usados pela API recebem a fábrica de conexão injetada;
  handlers não possuem modo legado sem autenticação e erros HTTP são
  estruturados e genéricos.
- Router, CORS e pipeline da API passaram a suportar Bearer/Authorization,
  logout e rotas protegidas de recrutadores. Testes unitários e de integração
  da T2 foram adicionados. A revisão pré-commit removeu o bypass sem sessão,
  endureceu IDs/`idEmpresa`, cobriu erros 500 genéricos e rollback de conflitos;
  nenhum schema público foi alterado.
- T4 frontend documentada: contratos de sessão, recrutador, curtida, match e
  vaga foram implementados no repositório frontend sem alterar o backend.
  A revisão registrou allowlists, `dataMatch: null` para legado e gate
  explícito da integração externa.
- T5 frontend registrada: telas reais protegidas, menus por sessão, gestão de
  recrutadores, curtidas/matches HTTP e testes DOM com `happy-dom`; T6 segue
  pendente. Commit T4 frontend confirmado como `36759bb`.
- Revisão T5 registrada: correções de alvo/pseudônimo/anonimato, feed real do
  recrutador, estados de gestão, corrida de sessão, logout, contadores e
  guards; 73 testes passaram, 1 integração opt-in foi ignorada e o build
  passou. O teste integrado usa mocks DOM/fetch, não navegador real.

### Corrigido

- Validação local: V2 aplicada após backup, preservando as contagens dos dados
  legados e restaurando o login; evidências HTTP/navegador registradas em
  `docs/specs/recrutador/validacao.md`, com autoria final Codex em `PROMPTS.md`.
- Corrigida a criação de `data_match` quando a curtida empresarial ocorre
  antes da curtida do candidato, incluindo repetição e concorrência.
- Revisada a spec do recrutador com contratos de status/erro, DTOs mínimos,
  regra de revelação agregada, curtida empresarial e distinção entre
  recrutador desativado e empresa/colegas ativos.

### Removido

- Nada removido.
