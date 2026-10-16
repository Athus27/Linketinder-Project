# Linketinder-Project

Aplicacao em Groovy para o MVP do Linketinder, um sistema simples de contratacao inspirado na ideia de combinar candidatos e empresas por competencias.

- **Trello Backlog:** https://trello.com/b/RVX7qZoN/linketinder

- **Autor: Athus**

## Funcionalidades

- Lista candidatos pre-cadastrados.
- Lista empresas pre-cadastradas.
- Persiste os dados dos candidatos no PostgreSQL.
- Persiste os dados das empresas no PostgreSQL.
- Cadastra novos candidatos pelo terminal.
- Cadastra novas empresas pelo terminal.
- Armazena competencias de candidatos e empresas.
- Valida formato de CPF, CNPJ e CEP.
- Mantem vagas associadas as empresas.
- Permite que candidatos curtam vagas.
- Permite que empresas curtam candidatos.
- Identifica match quando candidato e empresa se curtem para a mesma vaga.

## Estrutura Principal

- `App.groovy`: ponto de entrada da aplicacao, cria o Scanner e o LeitorConsole e inicia o menu principal.
- `view/console/MenuConsole.groovy`: direciona para os menus de candidatos, empresas, vagas e competencias.
- `CandidateConsole`, `CompanyConsole`, `VagaConsole` e `CompetenceConsole`: organizam os fluxos de cada menu.
- `view/console/input/types`: readers responsaveis por coletar os dados pelo terminal.
- `view/console/input/utils/formatter`: formatacao dos dados para exibicao no terminal.
- `controller`: faz a comunicacao entre os consoles e os DAOs.
- `dao`: executa os comandos SQL e controla as transacoes.
- `ConexaoDB.groovy`: carrega as configuracoes de conexao e abre conexoes com o PostgreSQL.
- `User.groovy`: classe base com atributos comuns, como nome, email, estado, CEP, descricao e competencias.
- `UserCandidate.groovy`: representa um candidato, com CPF e idade.
- `UserCompany.groovy`: representa uma empresa, com CNPJ, pais e vagas.
- `Vaga.groovy`: representa uma vaga publicada por uma empresa, com titulo, descricao e competencias esperadas.
- `CurtidaDAO.groovy`: registra as curtidas e identifica o match usando os dados do banco.

## Atualização: Banco de dados PostgreSQL
foi desenhado o **DER** do linketinder, utilizando o **dbdiagram.io**, a imagem gerada pelo diagrama está em `docs/derLinketinder.png`
- link: https://dbdiagram.io/d/Linketinder-6abbe06e5869425612cd1b34
- Os codigos SQL para criar as tabelas estão em `sql/Linketinder.sql`
- Para armazenar as senhas utilizei o `pgcrypto`, que é uma extensão do PostgreSQL que acrescenta funções de criptografia e hash, a principal função utilizada é a crypt():
```sql
crypt('Senha123', gen_salt('bf'))
```
> esse **gen_salt('bf')** gera um valor aleatório chamado salt e seleciona o algoritmo bcrypt, e o `crypt()` usa a senha e o salt para produzir o hash que será gravado. Tudo isso é executado dentro de `BEGIN` e `COMMIT`, que são clausulas **DTL**, ou grava tudo ou não grava nada!

## Atualizacao: Curtidas e Match

Nesta versao foi implementado o sistema de curtidas do Linketinder.

O candidato pode selecionar uma vaga e curtir essa oportunidade. A empresa pode selecionar uma de suas vagas e curtir um candidato. Quando os dois lados curtem a mesma relacao de candidato e vaga, o sistema identifica o match e exibe uma mensagem no terminal.

Na versao inicial, as curtidas eram armazenadas em memoria durante a execucao do programa. Agora elas sao persistidas na tabela `Curtida` do PostgreSQL utilizando JDBC. Cada registro relaciona um candidato e uma vaga, guarda a curtida de cada lado e registra a data quando acontece o match.

## Atualizacao: Integracao entre PostgreSQL e backend

Nesta etapa comecei a integrar o backend do Linketinder com o PostgreSQL utilizando JDBC, sem utilizar JPA ou Hibernate.

Separei as responsabilidades entre `view`, `controller` e `dao`. O `MenuConsole` coleta e exibe os dados, os controllers fazem a comunicacao com os DAOs e os DAOs executam os comandos SQL.

Foram implementadas as seguintes funcionalidades:

- Cadastro e listagem de candidatos no banco de dados.
- Cadastro e listagem de empresas no banco de dados.
- Cadastro, busca e listagem de competencias.
- Relacao N:N entre candidatos e competencias utilizando `CandidatoCompetencia`.
- Cadastro e listagem de vagas.
- Relacao N:N entre vagas e competencias utilizando `VagaCompetencia`.
- Relacao 1:N entre empresa e vagas utilizando `Vaga.id_empresa`.
- Feed anonimo de candidatos, sem exibir os dados pessoais.
- Feed anonimo de vagas, sem exibir o nome da empresa.
- Validacao de CPF, CNPJ, CEP e tamanho minimo da senha.
- CRUD de candidatos, empresas, competencias e vagas.
- Curtidas de candidatos e empresas persistidas no PostgreSQL.
- Identificacao e registro do match quando os dois lados curtem a mesma relacao entre candidato e vaga.

No cadastro de candidato, a aplicacao cria o usuario, cria o candidato e relaciona suas competencias dentro da mesma transacao. No cadastro de empresa, cria o usuario e utiliza o ID retornado para criar a empresa.

Para cadastrar uma vaga, o usuario seleciona uma empresa ja cadastrada, informa titulo, descricao e local, depois adiciona as competencias. Caso a competencia nao exista, ela e criada antes de gerar o relacionamento com a vaga.

Foi utilizado `commit` para confirmar as operacoes e `rollback` para desfazer tudo quando ocorre algum erro durante o cadastro.

Os arquivos relacionados ao banco estao em:

- `sql/Linketinder.sql`: criacao das tabelas e relacionamentos.
- `sql/DadosIniciais.sql`: candidatos, empresas, competencias e vagas ficticias.
- `docs/derLinketinder.png`: imagem atualizada do diagrama.

O menu atual permite listar, cadastrar, editar e remover candidatos, empresas e vagas. Para competencias, permite listar, editar e remover, alem de cria-las durante o cadastro de candidatos e vagas. Tambem permite visualizar os feeds anonimos e registrar curtidas e matches persistidos no banco de dados.

## Atualizacao: Boas Praticas - Clean Code e SOLID (K2-T1)

Nesta etapa refatorei a organizacao do backend para separar melhor as responsabilidades e facilitar a leitura do codigo.

O `App` ficou responsavel por iniciar a aplicacao. Removi dele o fluxo antigo que mantinha os dados em memoria, pois a versao atual utiliza o PostgreSQL. A classe `Curtida` tambem foi removida porque nao era mais utilizada pelo fluxo atual.

Dividi o `MenuConsole`, que antes concentrava as operacoes, em consoles de candidatos, empresas, vagas e competencias. Agora o menu principal direciona para cada um deles, e cada opcao chama um metodo com o fluxo correspondente.

As principais mudancas foram:

- Separei a leitura dos campos em readers, como `CandidateReader`, `CompanyReader` e `VagaReader`.
- Separei a formatacao dos dados em formatters, deixando os consoles responsaveis por conduzir os fluxos do terminal.
- Passei o mesmo `LeitorConsole` pelo construtor para compartilhar a entrada do terminal, sem criar um Scanner para cada menu.
- Utilizei atributos e metodos privados para os detalhes internos dos consoles, mantendo acessiveis os metodos utilizados por outras classes.
- Mantive os retornos antecipados quando o usuario cancela uma selecao, evitando continuar o cadastro, edicao ou remocao sem um registro selecionado.
- Reaproveitei a filtragem das vagas por empresa na consulta e na selecao de vaga para curtida, evitando repetir essa regra.
- Corrigi o fechamento do arquivo `db.properties` com `try/finally`, para fechar o stream mesmo quando ocorre um erro na leitura.
- Removi as dependencias diretas de Guava e JUnit 4, que nao eram utilizadas pelo codigo e pelos testes atuais.

Com essa separacao, apliquei o principio da responsabilidade unica (SRP) de forma pratica: leitura, formatacao e fluxo do menu ficaram em classes com tarefas mais claras. A extracao dos metodos tambem deixou os blocos de `case` menores e mais faceis de entender. Fiz essas melhorias por partes, seguindo a regra do escoteiro, aproveitando o codigo que ja existia.

## Atualizacao: Organizacao dos testes

Adaptei os testes que antes chamavam o `MenuConsole` para testar diretamente os readers e o formatter que receberam esses metodos. Mantive as verificacoes dos campos lidos e da exibicao dos dados do candidato sem mostrar a senha.

Separei os testes em duas suites:

- `app/src/test/groovy`: testes unitarios dos modelos, readers, formatter e DAOs com o banco simulado.
- `app/src/integrationTest/groovy`: testes de integracao da conexao com o PostgreSQL e da listagem das tabelas.

Na validacao desta etapa, passaram 33 testes unitarios e 2 testes de integracao com o PostgreSQL.

Para executar os testes unitarios:

```bash
./gradlew :app:test
# Executa os testes unitarios do modulo app.
```

Para executar os testes de integracao, o PostgreSQL precisa estar disponivel e o `db.properties` configurado no classpath:

```bash
./gradlew :app:integrationTest
# Executa os testes de integracao com o PostgreSQL.
```

Para gerar o build e executar as duas suites:

```bash
./gradlew :app:build :app:integrationTest
# Gera o build e executa os testes unitarios e de integracao.
```

## Como Executar

No terminal, dentro da pasta do projeto:

```bash
./gradlew run
# Inicia a aplicacao pelo terminal.
```

## Tecnologias

- Groovy
- Gradle
- PostgreSQL
- JDBC
- Spock para os testes
- Scanner para entrada de dados pelo terminal
- ArrayList utilizado na versao inicial para armazenar os dados em memoria

## Observacoes da versao inicial

Na versao inicial, os dados eram armazenados apenas em memoria. Ao encerrar o programa, os cadastros feitos pelo terminal nao eram salvos em arquivo ou banco de dados.

Na versao atual, candidatos, empresas, competencias, vagas, curtidas e matches sao persistidos no PostgreSQL utilizando JDBC.
