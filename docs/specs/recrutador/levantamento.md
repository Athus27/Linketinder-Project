# Levantamento factual — recrutador

Este documento é somente levantamento para a etapa 1. Não é spec, plano ou
decisão aprovada.

## O que existe

- **Backend/modelo:** Groovy/Java 21, Gradle, `HttpServer`, JDBC/PostgreSQL.
  `User` é base de candidato e empresa; `Candidate` tem CPF, nascimento,
  formação, descrição e competências; `Company` tem CNPJ, descrição e vagas;
  `Vaga` tem empresa, título, descrição, local e competências; `Curtida` guarda
  as duas curtidas por par candidato-vaga e `data_match`.
- **Backend/HTTP:** há GET de candidatos, empresas e vagas, POST de cadastros,
  DELETE de candidato e POST de login. Handlers chamam controllers e DAOs.
  Há `CurtidaDAO`/`CurtidaController`, mas nenhuma rota HTTP de curtida foi
  registrada. GET de candidatos seleciona nome, sobrenome, nascimento, CPF,
  descrição, e-mail, formação, país, CEP e competências; GET de empresas
  seleciona nome, CNPJ, e-mail, descrição, país e CEP; vagas incluem nome da
  empresa.
- **Autenticação:** login compara senha com hash no PostgreSQL e devolve ID,
  nome e tipo. Não há sessão, token, cookie ou autorização subsequente; DELETE
  de candidato e listagens não exigem login.
- **Frontend:** TypeScript/Vite, DOM, services `fetch`, controllers e views;
  `npm run dev`, `npm run build`, `npm run test:run`. Candidatos e empresas,
  login e GETs usam a API. Vagas, curtidas e matches continuam parcialmente
  locais. `LinketinderApp` persiste candidatos, empresas, vagas e curtidas em
  `localStorage`; o match local é as duas flags verdadeiras no mesmo par.
- **Telas:** cards de candidato podem ser anônimos, mas a tela de feed e
  formulários de curtida usam nomes; o card de vaga mostra título, descrição e
  competências. Os services de candidato/empresa reconstroem modelos com CPF,
  nascimento, e-mail/CNPJ recebidos da API.
- **Testes/documentação:** existem testes Spock unitários e de integração no
  backend e testes Vitest de controllers, services e views no frontend.
  README e docs descrevem a integração atual, inclusive que login não é
  sessão e que curtidas/matches não estão completamente integrados.

## Riscos factuais

1. Identidade e identificadores pessoais são retornados em GETs antes de
   qualquer match; o frontend também os mantém/renderiza. Isso contraria a
   restrição de anonimato a ser definida para o recrutador.
2. Descrição, local, CEP, idade/nascimento e e-mail aparecem em caminhos que
   ainda não têm política de exposição por estado de match; experiência não é
   um campo explícito do modelo atual.
3. Curtidas locais podem ser alteradas no navegador e não representam
   autorização ou estado confiável do servidor; as rotas HTTP correspondentes
   ainda não existem.
4. O login sem sessão/token deixa endpoints e IDs acessíveis sem prova de ator,
   inclusive operações administrativas/destrutivas.
5. A branch do frontend é uma dependência recente ainda não incorporada à main
   remota; contratos backend/frontend podem divergir e precisam ser tratados
   sem substituir trabalho existente.

## Perguntas obrigatórias antes da spec

- Quem pode cadastrar, editar, desativar e remover candidato, empresa, vaga e
  recrutador? Qual autorização de cada operação?
- Uma empresa pode ter várias vagas? Quem cria cada vaga e como o vínculo é
  validado?
- O voto da empresa é por vaga e candidato? Pode haver repetição pelos mesmos
  atores, revogação ou alteração de voto?
- O que significa “dois recrutadores” no domínio: dois usuários da mesma
  empresa, duas empresas ou outro papel? Como o segundo voto participa do
  match?
- Qual é o escopo de vagas e candidatos que cada papel pode consultar?
- Quais campos exatos são visíveis antes do match e quais contatos são liberados
  depois? Confirmar proibição permanente de CPF, CNPJ, nascimento, senha e
  hash.
- Quais validações de identidade, e-mail, senha, competências, formação,
  experiência, empresa, vaga e IDs são obrigatórias e quais mensagens/status
  devem ser públicos?

## Alternativas recomendadas e riscos

- **Servidor como fonte única de curtidas/matches, com sessão e autorização:**
  recomendado; exige contrato HTTP e migração de estado local, além de decisões
  sobre sessão e dados legados.
- **Manter `localStorage` e apenas ocultar campos na tela:** baixo esforço, mas
  não protege respostas HTTP, é manipulável e não cria match confiável; não
  atende ao objetivo de segurança.
- **Anonimizar somente no frontend:** não recomendado, pois a API continuaria
  entregando identidade e permitiria inspeção direta; pode servir apenas como
  camada adicional após correção do contrato.
