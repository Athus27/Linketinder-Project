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

## Referências da feature

- Issue: https://github.com/Athus27/Linketinder-Project/issues/3
- Sessão da issue: `8d5762a3-ba6d-4a38-81bf-e043305f0332`.
- Preparação já commitada: backend `d1ab06e` e frontend `3fbb374`.
- Após estes ajustes, a spec está revisada tecnicamente pelo Codex sob
  delegação explícita; não está aprovada pelo Tutor. A validação de negócio
  com Athus/Tutor permanece pendente antes da entrega.
