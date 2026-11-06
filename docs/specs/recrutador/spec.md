# Spec — recrutador

**Status:** revisada tecnicamente pelo Codex sob delegação explícita de Athus,
não aprovada pelo Tutor. A validação de negócio com Athus/Tutor permanece
pendente antes da entrega. Nenhum código, migração ou teste faz parte deste
artefato.

## Contexto e objetivo

O Linketinder já possui usuários candidatos e empresas, vagas e a persistência
de curtidas, mas não possui o papel recrutador integrado: não há sessão,
autorização subsequente ao login nem rotas HTTP de curtida/match. GETs atuais
também expõem identidade e dados pessoais antes do match, e parte do frontend
mantém estado confiável apenas em `localStorage`.

O objetivo é especificar o recrutador como colaborador de uma empresa,
preservar anonimato por allowlist no servidor e integrar autenticação, vagas,
curtidas e matches reais nos dois repositórios. A API será a fonte de verdade;
as demos locais continuam isoladas e não são mecanismo de autorização.

## Atores e autorização

- **Candidato autenticado:** consulta oportunidades anonimizadas, curte como
  ele próprio e consulta seus matches. Não escolhe outro candidato, empresa ou
  recrutador. Pode excluir apenas o próprio cadastro.
- **Empresa autenticada:** cria e administra suas vagas, cria/lista/desativa
  logicamente seus recrutadores e consulta dados de gestão de seus
  colaboradores. Não administra recrutadores de outra empresa.
- **Recrutador ativo autenticado:** pertence a uma única empresa, analisa todas
  as vagas dela, consulta candidatos anonimizados, registra a curtida da
  empresa no par candidato-vaga e consulta matches dessa empresa. Não
  cria/edita/exclui vagas nem recruta outro usuário.
- **Serviço/servidor:** deriva ator, papel e empresa da sessão e valida o
  vínculo empresa-vaga-candidato. Nunca confia em menu, ID de perfil,
  `localStorage` ou campos de identidade enviados pelo cliente.

Somente o recrutador desativado fica impedido de operar. A empresa e os demais
recrutadores ativos permanecem operacionais. Sessões existentes do recrutador
desativado deixam de autorizar operações imediatamente; o login também recusa
esse usuário inativo.

## Modelo de dados e relações

O modelo existente `User(id, nome, email, senha hash, ...)` é reutilizado.
Adicionar, em migração versionada V2 aditiva, `Recruiter`:

| Campo | Regra |
|---|---|
| `id_recrutador` | PK e FK 1:1 para `User`; identidade técnica do recrutador |
| `id_empresa` | FK obrigatória para `Company`; uma empresa tem 1:N recrutadores |
| `area` | obrigatória, texto descritivo |
| `descricao` | obrigatória, texto profissional |
| `cpf` | 11 dígitos normalizados, UNIQUE, nunca retornado |
| `data_nascimento` | data ISO real, >=18 anos, não futura, nunca retornada |
| `ativo` | obrigatório, inicia verdadeiro; desativação lógica |

Para recrutadores, país e CEP pessoais em `User` podem ser `NULL`; cadastros
legados não são retrovalidados nem destruídos. A senha continua armazenada
somente como hash compatível com o padrão existente (`pgcrypto`/bcrypt).

`Curtida` continua sendo única por `(id_candidato, id_vaga)` e contém as flags
da curtida do candidato e da empresa, `data_match` inicializada uma única vez e
o primeiro `id_recrutador_autor` quando a curtida empresarial for criada.
Repetições de recrutadores da mesma empresa não criam linha, voto, match ou
timestamp adicional. Constraints/transação devem tornar isso idempotente sob
concorrência. Vaga pertence a Company; Candidate pertence a User; o par só é
válido quando a vaga e o candidato existem e a empresa do recrutador é a dona
da vaga.

## Regras de negócio e validação

1. E-mail é `lower(trim(valor))`, globalmente único e de formato válido.
2. CPF de recrutador aceita apenas 11 dígitos após normalização; rejeita os
   11 dígitos iguais e dígitos verificadores inválidos.
3. Nascimento aceita somente data civil real `yyyy-MM-dd`, sem futuro e com
   idade mínima de 18 anos na data da operação.
4. Senha de recrutador tem 8–72 bytes UTF-8 e pelo menos uma letra e um
   número; nunca é devolvida nem registrada em logs.
5. Nome: obrigatório, após trim, 2–120 caracteres. Área: obrigatória, após
   trim, 2–100 caracteres. Descrição: obrigatória, após trim, 1–2.000
   caracteres. O texto livre não promete moderação automática: clientes e
   fixtures devem evitar PII, e a política de exposição limita seu retorno.
6. Falhas de validação retornam `400`; token ausente, inválido ou expirado,
   recrutador inativo e login inválido retornam `401`; papel não permitido e
   candidato forjado retornam `403`; recurso inexistente ou fora da empresa
   retorna `404` para ocultar sua existência; e-mail/CPF duplicados retornam
   `409`; falha inesperada retorna `500` genérico. Todo erro usa exatamente
   `{erro:string,codigo:string}`, sem valores sensíveis.
7. Uma curtida repetida retorna sucesso idempotente sem sobrescrever o primeiro
   autor. A operação é recusada se o recrutador não for ativo, a vaga não for
   da empresa, ou o candidato/par não estiver autorizado.
8. Match só existe quando as flags de candidato e empresa ficam verdadeiras no
   mesmo par. O timestamp é criado uma vez. Não há revogação nesta feature.

## Anonimato e projeções

DTOs/projeções no servidor usam allowlist; ocultar campos apenas no HTML,
JavaScript ou `localStorage` não atende. GETs públicos e perfis alheios não
carregam campos ocultos. Título, competências e, quando aplicável, formação e
experiência permitida podem aparecer; descrição/local de vaga devem respeitar
a política e não devem prometer ausência automática de PII.

| Estado / ator | Pode ver | Não pode ver |
|---|---|---|
| Antes do match, qualquer consulta de candidato | ID, competências e formação; pseudônimo opcional | nome, sobrenome, e-mail, CPF, nascimento, idade, CEP, descrição livre potencialmente identificável |
| Antes do match, qualquer consulta de empresa/vaga | ID da empresa, título e competências da vaga, demais atributos explicitamente anonimizados | nome, e-mail, CNPJ, identidade da empresa, identidade de candidato/recrutador |
| Após match, empresa correspondente e seus recrutadores | nome, sobrenome, e-mail e descrição profissional do candidato quando houver match autorizado pela empresa | CPF, nascimento, idade, CEP, senha/hash; dados de outra empresa |
| Após match, candidato titular | nome e e-mail da empresa do par | CNPJ, dados pessoais de recrutadores, matches de outra empresa |
| Gestão da empresa | nome, e-mail, área e descrição dos próprios colaboradores | CPF, nascimento, senha/hash e colaboradores de outra empresa |
| Qualquer estado | própria identidade autenticada no header pode ser usada para a sessão | identidade de terceiros fora da matriz acima |

Uma curtida da empresa não revela identidade ao candidato antes do match.
“Depois” exige match do mesmo candidato e vaga; match com outra empresa não
libera contato. O contato pós-match do recrutador é representado pela empresa,
não por seus dados pessoais.

## Contratos HTTP e telas

Todos os endpoints protegidos exigem `Authorization: Bearer <token>`. O token
é opaco, imprevisível, mantido somente em memória no frontend, válido por
quatro horas, revogado no logout e invalidado ao reiniciar a API. A resposta
de login retorna token e apenas `id`, `nome`, `tipo` e, quando pertinente,
`idEmpresa`; nunca CPF, CNPJ, nascimento, senha ou hash.

- `POST /api/auth/login`: autentica e cria sessão; inativo não entra e login
inválido retorna `401`; sucesso retorna `200`.
- `POST /api/auth/logout`: revoga a sessão corrente; token desconhecido é
  idempotente e retorna `204`.
- `GET /api/recrutadores`: somente empresa autenticada, lista seus
  colaboradores com projeção de gestão; retorna `200`.
- `POST /api/recrutadores`: somente empresa autenticada; cria `User` +
  `Recruiter` atomicamente e vincula à empresa da sessão, sem aceitar
  `id_empresa` confiável do corpo; retorna `201`.
- `DELETE /api/recrutadores/{id}`: somente empresa dona; desativa
  logicamente, sem apagar histórico; retorna `204`.
- `GET /api/vagas?minhas=true`: empresa ou recrutador ativo, somente vagas da
  empresa derivada da sessão. Listas públicas, se mantidas, são anônimas.
- `POST /api/curtidas`: candidato envia `{idVaga}`; `idCandidato` é opcional,
  mas, se fornecido, deve ser igual ao próprio ID, ou responde `403`.
  Empresa ou recrutador envia `{idVaga,idCandidato}`, com candidato obrigatório.
  `idEmpresa` e `idRecrutador` não são autoridade; se fornecidos diferentes do
  ator, respondem `403`. Empresa proprietária também pode curtir. Retorna
  `200`.
- `GET /api/matches`: somente titular autenticado, com projeção segura e
  filtrada pelo par/empresa autorizado; retorna `200`.

Quando um legado possuir as duas flags de match verdadeiras, mas
`data_match` for `NULL`, o contrato retorna `dataMatch: null`: o cliente não
inventa uma data histórica. Strings de data presentes devem ser ISO válidas;
o frontend exibirá “Data não registrada” para o valor nulo.

Os nomes podem ser ajustados na implementação somente para manter convenções
existentes, sem multiplicar endpoints nem relaxar as regras. O frontend deve
oferecer login de recrutador, menus por papel, gestão empresarial, vagas da
empresa, candidatos anônimos, curtida/match e estados loading/erro/vazio. A
criação de vaga na navegação real usa API, sem fallback local. `LinketinderApp`
permanece demo isolada, com anonimato e regras, sem autorizar ações reais.

### DTOs mínimos

- Recrutador de gestão: `{id,nome,email,area,descricao,idEmpresa,ativo}`.
- Candidato antes do match: `{id,competencias,formacao}`; pseudônimo é
  opcional. Após match, acrescenta `nome,sobrenome,email,descricao`.
- Empresa antes do match: `{id}`; após match: `{id,nome,email}`.
- Vaga: `{id,idEmpresa,titulo,descricao,competencias}`. Nome/e-mail da
  empresa só aparecem pós-match; vagas próprias da empresa ou recrutador podem
  conter contexto da própria empresa, sem expor candidatos sem match.
- Match: `{idCandidato,idVaga,dataMatch,candidato/empresa}`, com projeções
  escolhidas conforme o ator autorizado.

Um GET de candidato sem vaga explícita aplica a regra agregada: empresa ou
recrutador vê contato se existir ao menos um match confirmado entre esse
candidato e qualquer vaga daquela empresa; candidato vê contato da empresa se
tiver match com ao menos uma vaga dela. Sem match, nunca há contato. O GET de
matches continua estrito aos pares exatos.

## Casos de borda e compatibilidade

- Concorrência entre dois recrutadores da mesma empresa usa unicidade e
  transação no banco; somente o primeiro autor permanece.
- Repetir POST após timeout não duplica voto, match ou timestamp.
- Desativar recrutador bloqueia login e operações de sessões já abertas.
- Um ID válido de vaga de outra empresa não deve permitir inferência ou ação.
- Dados legados podem conter campos fora das novas regras; não são destruídos
  nem retrovalidados, mas novas projeções ainda aplicam anonimato.
- Fixtures e documentos usam somente dados sintéticos, como
  `recrutador@example.test`; não usar CPF, CNPJ ou dados reais.
- V2 é additive e não reaplica o schema inicial. Validação futura deve usar
  database/schema temporário, após conferir o alvo, sem resetar banco existente
  nem imprimir configuração ou dados pessoais.

## Critérios de aceite

1. **Cadastro autorizado:** Dado uma empresa autenticada, quando ela envia
   nome, e-mail, senha, área, descrição, CPF e nascimento válidos, então o
   servidor cria `User` e `Recruiter` ligados à empresa da sessão e retorna
   apenas a projeção permitida.
2. **Relação 1:N:** Dado duas criações válidas pela mesma empresa, quando a
   lista é consultada, então ambas aparecem ligadas somente a essa empresa; um
   recrutador nunca pertence a duas empresas.
3. **Campos obrigatórios:** Dado nome, área ou descrição ausente, vazio ou
   fora do limite, quando o cadastro é enviado, então responde `400` com código
   estável e não persiste parcialmente.
4. **Validação de identidade:** Dado e-mail inválido/duplicado, CPF repetido,
   CPF inválido, nascimento inválido, futuro ou menor de idade, quando o
   cadastro é enviado, então responde `400` ou `409` conforme a causa e não
   expõe o valor sensível.
5. **Senha:** Dada senha fora de 8–72 bytes UTF-8 ou sem letra/número, quando
   o cadastro é enviado, então responde `400` e jamais devolve senha/hash.
6. **Autorização de gestão:** Dado recrutador autenticado, quando tenta criar,
   listar ou desativar recrutador, então responde `403`; dada empresa alheia,
   quando tenta desativar um ID, então não altera o colaborador.
7. **Desativação:** Dado recrutador ativo desativado pela própria empresa,
   quando ele tenta login ou usa sessão anterior, então recebe `401`/`403` e
   nenhuma operação é executada.
8. **Vínculo de vaga:** Dado recrutador ativo, quando lista vagas próprias,
   então vê todas e somente as vagas da empresa da sessão; ID de vaga de outra
   empresa não pode ser usado para curtir.
9. **Curtida do candidato:** Dado candidato autenticado, quando curte uma vaga,
    então o servidor usa seu próprio ID; um `idCandidato` diferente do próprio
    recebe `403` e nunca produz voto em nome dele.
10. **Curtida empresarial:** Dado empresa proprietária ou recrutador ativo,
    quando envia `idVaga` e `idCandidato` de par válido, então a curtida
    representa a empresa; IDs de empresa/recrutador divergentes do ator recebem
    `403` e vínculo alheio não revela existência.
11. **Repetição e concorrência:** Dado dois recrutadores da mesma empresa
    enviando simultaneamente a mesma curtida, quando ambas concluem, então há
    uma única curtida empresarial, um único par/match possível e o primeiro
    autor não é sobrescrito.
12. **Match:** Dado flags verdadeiras do candidato e da empresa no mesmo par,
    quando a segunda operação conclui, então `data_match` é criado uma vez;
    outro par ou outra empresa não revela contato.
13. **Pós-match restrito:** Dado match válido, quando a empresa correspondente
    ou seus recrutadores consultam, então veem apenas contato profissional
    previsto; candidato vê nome/e-mail da empresa; CPF, CNPJ, nascimento,
    senha, hash e dados do recrutador continuam ausentes. Em GET de candidato
    sem vaga explícita, basta um match confirmado com qualquer vaga daquela
    empresa; GET de matches continua restrito ao par exato.
14. **Anonimato antes do match:** Dado GET público ou consulta sem match,
    quando a resposta é serializada e renderizada, então só contém os campos
    anonimizados allowlistados, sem PII em propriedades ocultas, `localStorage`
    ou HTML.
15. **Sessão:** Dado login válido, quando o cliente chama endpoint protegido
    com Bearer válido, então o servidor autoriza pelo estado da sessão; após
    logout, expiração de quatro horas ou reinício da API, o mesmo token recebe
    `401`.
16. **Contratos e regressão:** Dado empresa autenticada, quando cria vaga pela
    navegação real, então a API persiste e a lista usa a resposta do servidor,
    sem fallback local; cadastros, consultas e contadores existentes continuam
    operando com DTOs anonimizados e regras antigas não são destruídas.
17. **Erros e status:** Dado token ausente/inválido/expirado/inativo, login
    inválido, papel proibido, recurso inexistente, duplicata ou erro inesperado,
    quando a API responde, então usa respectivamente `401`, `403`, `404`,
    `409` ou `500`, conforme a regra, e todo erro tem apenas
    `{erro:string,codigo:string}` sem valores.
18. **Camadas:** Dado qualquer violação de autorização ou anonimato, quando a
    operação passa por handler, controller, DAO e frontend, então cada camada
    preserva a decisão do servidor, sem confiar em seletor de menu ou ID do
    cliente.

## Impacto

Backend: novos contratos de sessão, recrutador, curtida/match, projeções
anonimizadas e relacionamento de dados; router, handlers, controllers, DAOs e
migração V2 serão afetados. Frontend: services, estado de sessão em memória,
menus por papel, gestão de recrutadores, vagas, curtidas/matches e telas de
loading/erro/vazio serão integrados à API. Integrações existentes de cadastro,
consultas, gráfico e contadores devem permanecer funcionais dentro das novas
allowlists. A futura implementação deve atualizar documentação e changelog de
cada repositório, mas esta etapa não cria plano, código, migração ou testes.

## Fora do escopo

JWT ou framework novo; produção; redefinição de senha; revogação/edição de
curtida; edição de vagas por recrutador; filtros, métricas, histórico avançado,
opcionais Plus e MCP de banco; moderação automática de texto livre; reset de
banco; migração destrutiva; autorização por `localStorage`; publicação de PR,
merge, entrega Talent ou aprovação do Tutor.

## Decisões pendentes e limitações

As decisões acima foram revisadas tecnicamente pelo Codex sob delegação
explícita de Athus para execução delegada; a validação de negócio com
Athus/Tutor permanece pendente antes da entrega. A spec não afirma que o código
atual já garante anonimato, sessão ou concorrência. Ainda precisam ser
detalhados na implementação os nomes finais dos DTOs, o mecanismo concreto de
armazenamento em memória das sessões e os limites de qualquer campo legado
adicional. Esses detalhes não podem enfraquecer a matriz, autorização derivada
do servidor, unicidade no banco ou proibição permanente de dados sensíveis.
