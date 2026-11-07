# Integração do Linketinder: frontend e backend

O [frontend TypeScript](https://github.com/Athus27/Linketinder-FrontEnd) usa
DOM nativo e fetch, sem framework de interface. O backend deste repositório
usa Groovy, HttpServer do JDK, JDBC e PostgreSQL, sem framework web.

## Responsabilidades

O frontend roda pelo Vite, normalmente em http://localhost:5173.
Main chama renderAll, que monta o cabeçalho, coordena a navegação e fornece
às páginas funções de consulta. Formulários usam controllers/services; os
services convertem os campos da interface para os contratos HTTP.

O backend roda normalmente em http://localhost:8080. Router registra os
contextos, CorsFilter trata a origem/preflight, handlers interpretam método,
JSON e sessão, controllers coordenam as regras e DAOs acessam o banco com JDBC.

O caminho de uma operação é: formulário/página → service HTTP (diretamente ou
via controller do frontend) → HttpServer/Router e filtro → handler →
controller do backend → DAO → PostgreSQL. Status e JSON retornam pelo caminho
inverso. O navegador nunca acessa o banco diretamente.

## Operações integradas

| Ação | Requisição | Resultado |
|---|---|---|
| Cadastro de candidato | POST /api/candidatos | Identidade, perfil e competências no banco |
| Cadastro de empresa | POST /api/empresas | Identidade e perfil no banco |
| Login | POST /api/auth/login | Perfil e token de sessão |
| Logout | POST /api/auth/logout | Token revogado e estado local limpo |
| Candidatos | GET /api/candidatos | Projeção anônima ou contato autorizado após match |
| Excluir próprio candidato | DELETE /api/candidatos/{id} | Exclusão autorizada pelo token do titular |
| Empresas | GET /api/empresas | Projeção conforme ator/match |
| Vagas | GET /api/vagas | Oportunidades sem identidade da empresa |
| Vagas próprias | GET /api/vagas?minhas=true | Vagas da empresa autenticada ou vinculada ao recrutador |
| Cadastro de vaga | POST /api/vagas | Empresa derivada da sessão |
| Gestão de recrutadores | GET/POST /api/recrutadores e DELETE por ID | Empresa lista, cria e desativa seus colaboradores |
| Curtida | POST /api/curtidas | Interesse no par candidato-vaga |
| Matches | GET /api/matches | Contatos liberados para o ator autorizado |
| Contadores/gráfico | GETs de empresas, candidatos e vagas | Quantidades e contagem de competências |

Vagas, curtidas e matches usam a API como fonte de verdade. A demo
LinketinderApp com localStorage permanece isolada: não é instanciada pelo
main nem autoriza operações do fluxo real.

## Cadastro e adaptação de dados

Os formulários públicos coletam dados de candidato/empresa. Os controllers
chamam os services, que convertem name, password e cepCode para nome, senha
e cep. O candidato usa data no input yyyy-MM-dd, convertida para dd/MM/yyyy
no POST. O recrutador usa yyyy-MM-dd diretamente.

O backend grava os dados compartilhados em User e o perfil na tabela
correspondente. O cadastro usa transação; as senhas são armazenadas como hash
pgcrypto, não texto puro, e não voltam nas consultas. Sucesso responde 201;
duplicidades de unicidade, 409 com erro genérico. A validação do recrutador
exige adulto, CPF válido e senha de 8–72 bytes UTF-8 com letra e número;
candidato e empresa mantêm o mínimo de seis caracteres.

Os exemplos completos e as diferenças entre os contratos estão em
[api.md](api.md); não use credenciais reais nos arquivos de requisição.

## Login, sessão e menus

1. LoginPage envia e-mail/senha por LoginController e LoginService.
2. AuthDAO compara o hash e identifica candidate, company ou recruiter ativo.
3. O servidor cria um token opaco com validade de quatro horas.
4. LoginService guarda token e perfil somente em memória com SessionService.
5. RenderAll recria Header com o usuário; cada perfil vê seu próprio menu.
6. Chamadas protegidas recebem Authorization: Bearer com o token atual.
7. O servidor resolve a sessão e revalida o usuário e seu vínculo no banco.

Apenas esconder botões não protege a API. O servidor impede, por exemplo,
um recrutador de criar vaga ou administrar outros recrutadores.

Logout revoga o token e limpa o estado da interface. Em falha de rede, o
frontend limpa o estado local e avisa que a revogação não foi confirmada.
Recarregar a página perde o token local, mas não revoga por si só o token no
servidor, que permanece sujeito ao TTL. Reiniciar a API invalida suas sessões.

Ao receber 401 de uma chamada da sessão atual, SessionService limpa o estado
e retorna à tela de login. Uma resposta antiga não deve apagar uma nova sessão.
Recrutador desativado perde acesso inclusive com token emitido anteriormente.

## Fluxo do recrutador e anonimato

1. A empresa faz login e abre Gestão de recrutadores.
2. Cadastra o colaborador; o vínculo vem da sessão, sem seletor de empresa.
3. O recrutador faz login e consulta todas as vagas da própria empresa.
   A área de recrutamento é descritiva, não filtra as vagas nesta versão.
4. Analisa candidatos com ID técnico, competências e formação, sem nome,
   e-mail, CPF, nascimento, endereço ou descrição livre antes do match.
5. Escolhe uma vaga própria e curte o candidato; o voto representa a empresa.
6. Outro recrutador da mesma empresa repetir não cria novo voto nem match.
7. Quando o candidato também curte a mesma vaga, ocorre o match.

Após o match, a empresa e seus recrutadores podem ver nome, sobrenome, e-mail
e descrição profissional do candidato. Essa liberação vale para a empresa
que tem match com o candidato em pelo menos uma de suas vagas, não para todos.
O candidato consulta nome/e-mail da empresa em Matches; não recebe dados
pessoais do recrutador. CPF, CNPJ, nascimento, senha e hash nunca aparecem
nas projeções de consulta.

CandidateService usa aliases Candidato #ID antes do match e só preenche
contato quando autorizado pela API. CompanyService mantém aliases na
listagem; MatchService fornece os contatos à página de matches.
Campos profissionais livres não possuem moderação automática: use dados
fictícios e não inclua informações identificáveis em textos públicos.

## Contadores e gráfico de competências

InfoSection consulta empresas, candidatos e vagas da API. Durante a consulta,
mostra carregamento; falhas aparecem como indisponibilidade, não como zero.
Não conta registros da demo local.

GraphSkillsPage recebe funções de busca e carrega vagas/candidatos em paralelo.
Visitante e candidato consultam oportunidades públicas; empresa/recrutador
usam vagas próprias. Chart.js mostra quantos candidatos possuem cada
competência exigida pela vaga selecionada. A comparação é por texto exato,
diferenciando maiúsculas e minúsculas; cada candidato conta uma vez por
competência, mesmo com repetição na sua lista.

Exemplo: uma vaga exige Groovy, Java e SQL. Se um candidato tem Groovy, dois
têm Java e nenhum tem SQL, as barras são 1, 2 e 0. Sem candidatos, a contagem
é zero; sem vagas, a página orienta cadastrar uma. Erros de consulta aparecem
como erro, sem inserir dados fictícios para disfarçar a falha.

Os dados são carregados ao abrir a página. Não há atualização em tempo real,
paginação ou endpoint agregado de estatísticas; a contagem acontece no frontend
sobre as listas recebidas.

## Configuração e execução local

Use Java 21 compatível com o Gradle, Node.js/npm e PostgreSQL com pgcrypto.

1. Inicie o PostgreSQL e configure app/src/main/resources/db.properties no
   backend. Não versione esse arquivo nem exponha seu conteúdo.
2. Para banco vazio, aplique sql/Linketinder.sql: o script completo inclui
   pgcrypto e o estado de V1/V2. Não aplique as migrações depois dele.
3. Para banco legado, confira o alvo e as duplicidades de e-mail, faça backup
   protegido e aplique somente as migrações que faltam, em transação, conforme
   o README. V2 não deve ser reaplicada; se o banco já está atualizado, não há
   SQL a executar. Os schemas temporários dos testes não atualizam o banco local.
4. Inicie o backend e o frontend em terminais separados.

No backend:

~~~bash
./gradlew run
# Inicia a API, normalmente em http://localhost:8080.
~~~

No frontend, configure .env na raiz:

~~~dotenv
VITE_API_URL=http://localhost:8080
~~~

~~~bash
npm install
# Instala as dependências do frontend.
npm run dev
# Inicia o Vite, normalmente em http://localhost:5173.
~~~

Se o frontend usar outra origem, ajuste FRONTEND_ORIGIN no backend e reinicie
a API. VITE_API_URL indica o destino das requisições; FRONTEND_ORIGIN indica
a origem autorizada a ler as respostas. Veja [cors.md](cors.md).

## Validação e limites

Backend: ./gradlew check executa testes unitários e de integração PostgreSQL.
Frontend: npm run test:run executa testes unitários/DOM; a consulta real é
opt-in com VITE_RUN_API_INTEGRATION=true. Esses testes não substituem testar
os fluxos no navegador. Evidências e limites observados estão em
[validacao.md](specs/recrutador/validacao.md).

Editar candidatos e editar/excluir vagas não estão integrados. A API não
expõe edição/exclusão de empresas nem PUT. Os formulários legados de edição
e a demo isolada não significam que essas operações estejam disponíveis.
O token em memória é perdido no reload; não há sessão persistente no browser.
O projeto é local de estudo, sem alegação de prontidão para produção.
