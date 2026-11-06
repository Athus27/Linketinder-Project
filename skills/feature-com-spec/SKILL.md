---
name: feature-com-spec
description: "Use para conduzir uma feature do Linketinder por contexto, especificação aprovada, plano, implementação incremental e validação."
---

# Feature com especificação

Use esta skill nos dois repositórios do Linketinder: o backend e o frontend têm
`AGENTS.md`, `CHANGELOG.md` e validações próprios. O backend concentra
`docs/specs/<feature>/spec.md`, `plano.md`, esta skill e `PROMPTS.md`.

## Etapa 1 — Contexto

Leia as instruções locais, documentação relevante, configuração sem segredos,
código, testes e histórico diretamente relacionado. Registre fatos verificados,
diferenças entre documentação e código, limitações e cenários não executados.
Não leia nem exponha `db.properties`, `.env`, credenciais ou dados pessoais.

## Etapa 2 — Especificação e aprovação

Antes de código, escreva `docs/specs/<feature>/spec.md` com:

- contexto, objetivo e fora do escopo;
- atores, autorização e regras de negócio;
- contratos HTTP e comportamento observável das telas;
- bordas, dados legados e compatibilidade;
- critérios Given/When/Then testáveis;
- decisões pendentes e perguntas ao Tutor.

A spec deve tratar anonimato das respostas HTTP e das telas, autorização,
empresa/recrutador, dados fictícios e segredos. A spec deve ser revisada
tecnicamente pelo Codex sob delegação de Athus e commitada antes do código.
Decisões de negócio propostas devem ser registradas honestamente como pendentes
de validação humana/Tutor; isso não impede a execução técnica delegada.

## Etapa 3 — Plano e implementação

Depois da revisão técnica, escreva `docs/specs/<feature>/plano.md`. Divida em tarefas
pequenas por camada e dependência real (contrato, persistência, serviço,
interface e testes). Cada tarefa deve listar arquivos, dependências, comandos e
verificações. O checklist de toda tarefa inclui:

- [ ] anonimato de respostas HTTP e telas, antes e depois do match;
- [ ] autenticação, autorização do ator e vínculo empresa/vaga/candidato;
- [ ] proteção de empresas, candidatos, recrutadores e segredos;
- [ ] dados fictícios em exemplos, fixtures e testes;
- [ ] testes de regressão e limitações de validação;
- [ ] atualização do `CHANGELOG.md` do repositório afetado e deste plano.

Com a spec revisada tecnicamente, faça um commit dela antes de qualquer código. Execute uma
única tarefa por vez; atualize changelog e plano ao concluí-la; rode as
verificações previstas; termine cada tarefa em um commit específico. Não exija
skills `superpowers` ou outras que não existam para o Copilot.

## Etapa 4 — Validação e relato

Backend: use `./gradlew check` (ou o teste mais específico que cubra a mudança).
Frontend: use `npm run test:run` e `npm run build` conforme o escopo. Diferencie
teste automatizado, build, verificação de tela e cenário não executado. Não
alegue segurança ou anonimato apenas porque um teste indireto passou.

## Modelo mínimo

`spec.md` deve conter `Contexto e objetivo`, `Atores e autorização`, `Regras de
negócio`, `Contratos e telas`, `Casos de borda`, `Critérios de aceite`,
`Impacto`, `Fora do escopo` e `Decisões pendentes`.

`plano.md` deve conter tarefas numeradas, dependências, arquivos, comandos,
checklists de segurança/regressão/changelog e estado de conclusão.
