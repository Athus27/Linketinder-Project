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
