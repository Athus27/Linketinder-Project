# Validação local — 07/11/2026

## Pendências corrigidas

- O login retornava 500 porque `public.Recruiter` ainda não existia. A V2 já
  versionada foi aplicada no banco local `linketinder`, após backup custom
  PostgreSQL protegido (pasta 0700 e arquivo 0600, ignorados pelo Git).
  Contagens antes/depois: User 15, Candidate 7, Company 7, Vaga 7, Curtida 1.
  Nenhum cadastro legado foi removido ou alterado pela operação.
- O CSS `display: flex` exibia botões com `hidden`. A regra global de
  visibilidade foi corrigida. O teste com CSS real falhou com `flex` antes da
  correção e passou depois; a confirmação também ocorreu no navegador.

## Navegador real

O login do candidato legado funcionou no ambiente principal. O restante usou
API em 8081, frontend em 5174 e schema temporário, com dados fictícios
`@example.test`, sem cadastrar pessoas no schema público.

Verificado: login de empresa, criação de recrutador pelo formulário, logout,
login de recrutador, vagas próprias, candidato exibido como pseudônimo com
formação/competências sem identidade, curtida e replay sem duplicação, login
do candidato, curtida recíproca, mensagem de match e contato da empresa.
Depois do match, o recrutador viu nome/e-mail/descrição profissional autorizados,
sem CPF, nascimento ou senha. Menus exibiram somente o papel autenticado.

Limite observado: o diálogo nativo de confirmação de desativação bloqueou o
controle do navegador. A desativação foi então validada pela API HTTP real:
204, token anterior invalidado com 401 e novo login recusado com 401.
Não declarar a confirmação visual desse diálogo como concluída.

## Verificações automatizadas

- Backend: `./gradlew check --rerun-tasks --console=plain`, Java 21 explícito;
  122 testes unitários e 14 de integração aprovados.
- Frontend: `VITE_RUN_API_INTEGRATION=true VITE_API_URL=http://localhost:8080
  npm run test:run` e `npm run build`: 75 testes aprovados, nenhum ignorado,
  incluindo consulta à API real e regressão da visibilidade com CSS efetivo.
- HTTP real no schema temporário: allowlist de gestão do recrutador,
  administração e criação de vaga negadas ao recrutador (403), match existente,
  desativação (204), invalidação da sessão/login (401) e logout revogando token.

Reverificação antes dos commits: backend com 122 unitários e 14 de integração
aprovados; frontend com 74 testes aprovados e a integração opt-in ignorada
(não habilitada nesta execução), além de build aprovado. O resultado de 75
testes acima corresponde à execução anterior com integração habilitada.

O schema temporário e os servidores de teste foram encerrados após a
validação. O ambiente principal permanece em 8080/5173. Backup e credenciais
não fazem parte dos commits. A revisão de negócio pelo Tutor, o checklist
completo dos 18 aceites e o fechamento documental da T6 não são comprovados
por este smoke test; a tarefa não foi entregue nem foi feito merge.
