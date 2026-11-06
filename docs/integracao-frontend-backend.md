# Integração do Linketinder: frontend e backend

Este documento descreve como o frontend TypeScript e o backend Groovy do Linketinder se comunicam, quais operações já usam a API e onde estão os limites atuais. O frontend fica no repositório [Linketinder-FrontEnd](https://github.com/Athus27/Linketinder-FrontEnd); o backend fica neste repositório.

## Aplicações

### Frontend

O frontend é uma aplicação TypeScript executada pelo Vite, normalmente em http://localhost:5173. A interface é construída com a API do DOM e organizada em páginas, componentes, controllers e services. Os formulários e páginas chamam services, que fazem requisições HTTP com fetch; o navegador nunca acessa o PostgreSQL diretamente.

Pontos principais:

- src/view: formulários, cabeçalho e páginas.
- src/controllers: coordena ações da interface.
- src/services: envia requisições HTTP e adapta os contratos da API aos modelos do frontend.
- src/renderAll.ts: cria controllers, monta o cabeçalho e troca as páginas.

### Backend

O backend é uma aplicação Groovy/JVM sem framework web, executada normalmente em http://localhost:8080. O HttpServer do JDK recebe HTTP; Router associa caminhos a handlers; handlers interpretam método, JSON e status; controllers chamam DAOs; os DAOs acessam PostgreSQL com JDBC.

~~~text
View no navegador
  → Service (fetch/JSON)
  → HttpServer + Router
  → Handler
  → Controller
  → DAO (JDBC)
  → PostgreSQL
~~~

## Operações integradas

| Ação na interface | Requisição | Resultado |
|---|---|---|
| Cadastrar candidato | POST /api/candidatos | Salva identidade, perfil e competências. |
| Cadastrar empresa | POST /api/empresas | Salva identidade e perfil da empresa. |
| Entrar | POST /api/auth/login | Confere senha e devolve ID, nome e tipo do perfil. |
| Visualizar candidatos | GET /api/candidatos | Busca a lista no PostgreSQL. |
| Gerenciar candidatos | GET /api/candidatos e DELETE /api/candidatos/{id} | Lista e exclui candidatos no PostgreSQL. |
| Visualizar empresas | GET /api/empresas | Busca a lista no PostgreSQL. |
| Visualizar vagas | GET /api/vagas | Busca a lista no PostgreSQL. |
| Visualizar contadores | GET /api/empresas, /api/candidatos e /api/vagas | Mostra o total de registros retornados pelo backend. |
| Gráfico de competências | GET /api/vagas e /api/candidatos | Conta candidatos por competência exigida na vaga selecionada. |

O cadastro de vagas pela interface, edição de candidatos, edição/exclusão de empresas e vagas, curtidas e matches ainda não estão completamente integrados; parte desses dados continua em localStorage.

## Contadores e gráfico de competências

A InfoSection consulta as três listas da API e mostra a quantidade de empresas, candidatos e vagas retornadas. Não usa os registros de demonstração do localStorage. Enquanto consulta, mostra carregamento; se uma consulta falhar, o contador correspondente fica indisponível, sem apresentar um zero enganoso.

Na página do gráfico, GraphSkillsPage recebe as funções de consulta dos controllers e carrega vagas e candidatos em paralelo. O seletor usa as vagas do banco; ao escolher uma vaga, CompetenceGraph usa Chart.js para mostrar quantos candidatos possuem cada competência exigida. Os services convertem competencias do JSON em skills nos modelos TypeScript.

Por exemplo: se a vaga exige Groovy, Java e SQL, e dois candidatos possuem Java, um possui Groovy e nenhum possui SQL, as barras mostram Groovy: 1, Java: 2 e SQL: 0. Cada candidato é contado uma única vez por competência. A comparação atual usa o texto exato da competência, diferenciando maiúsculas e minúsculas.

Sem vagas, a página informa que é necessário cadastrar uma; sem candidatos, as barras ficam em zero. Falhas de consulta mostram uma mensagem de erro, sem substituir os dados por registros locais. As listas são carregadas ao abrir a página; não há atualização automática em tempo real. A contagem acontece no frontend sobre as listas completas recebidas, não em um endpoint agregado de estatísticas.

## Fluxo de cadastro de empresa

1. A pessoa preenche nome, e-mail, CNPJ, país, CEP, descrição e senha no formulário.
2. CompanyController chama CompanyService.cadastrarEmpresa.
3. O service transforma os nomes do formulário (name, password, cepCode) para o contrato do backend (nome, senha, cep) e envia JSON.
4. CompanyHandler valida JSON e senha, CompanyController delega ao CompanyDAO e o DAO grava em User e Company dentro de uma transação.
5. O backend responde 201 Created. Repetição de e-mail, CPF ou CNPJ retorna 409 Conflict com mensagem genérica.

Exemplo de corpo enviado:

~~~json
{
  "nome": "Tapetes Bolinho",
  "email": "contato@example.com",
  "cnpj": "67.890.123/0001-45",
  "pais": "Brasil",
  "cep": "01020-000",
  "descricao": "Fabricante de tapetes personalizados",
  "senha": "senha-com-6-ou-mais"
}
~~~

As senhas têm no mínimo seis caracteres. O PostgreSQL armazena hash gerado por pgcrypto; senha em texto não é gravada nem devolvida.

## Fluxo de login

O formulário envia POST /api/auth/login com e-mail e senha. O DAO procura o e-mail sem diferenciar caixa nem espaços externos e compara a senha com o hash por crypt. Perfil de candidato ou empresa é identificado pela existência em User e Candidate ou Company.

~~~json
{
  "email": "CONTATO@example.com",
  "senha": "senha-com-6-ou-mais"
}
~~~

Uma resposta válida contém somente os dados públicos usados pela interface:

~~~json
{
  "id": 12,
  "nome": "Tapetes Bolinho",
  "tipo": "company"
}
~~~

Credenciais inválidas recebem 401 e uma mensagem genérica. Campos ausentes ou JSON inválido recebem 400. A senha e o hash nunca são incluídos na resposta.

O frontend mantém esse perfil apenas em memória: mostra o nome e o tipo no cabeçalho, abre o menu correspondente e limpa o estado ao sair ou atualizar a página. **Isto não é uma sessão autenticada:** o backend não emite token/cookie e os endpoints não exigem autenticação. A seleção de menu é somente comportamento de interface.

## Configuração e execução local

1. Inicie o PostgreSQL e configure app/src/main/resources/db.properties no backend, sem adicionar esse arquivo ao Git.
2. Para um banco novo, execute sql/Linketinder.sql e habilite pgcrypto. Para banco existente, confira duplicidades com lower(trim(email)) e aplique sql/migrations/V1__unique_user_email_normalized.sql somente se a consulta retornar zero.
3. Inicie o backend com ./gradlew run na raiz do projeto. O padrão é a porta 8080.
4. No frontend, configure VITE_API_URL=http://localhost:8080 em .env e rode npm run dev.
5. O backend permite por padrão a origem http://localhost:5173; se a porta/origem do Vite for diferente, configure FRONTEND_ORIGIN no backend.

O CorsFilter permite que o navegador leia as respostas da origem configurada. CORS não substitui login nem autorização. Os contratos e exemplos individuais estão em [api.md](api.md), e os detalhes de CORS em [cors.md](cors.md).
