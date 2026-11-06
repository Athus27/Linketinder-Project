# Changelog

## [Não lançado]

### Adicionado

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

- Router, CORS e pipeline da API passaram a suportar Bearer/Authorization,
  logout e rotas protegidas de recrutadores. Testes unitários e de integração
  da T2 foram adicionados. A revisão pré-commit removeu o bypass sem sessão,
  endureceu IDs/`idEmpresa`, cobriu erros 500 genéricos e rollback de conflitos;
  nenhum schema público foi alterado.

### Corrigido

- Revisada a spec do recrutador com contratos de status/erro, DTOs mínimos,
  regra de revelação agregada, curtida empresarial e distinção entre
  recrutador desativado e empresa/colegas ativos.

### Removido

- Nada removido.
