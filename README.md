# Linketinder-Project

Aplicacao em Groovy para o MVP do Linketinder, um sistema simples de contratacao inspirado na ideia de combinar candidatos e empresas por competencias.

- **Trello Backlog:** https://trello.com/b/RVX7qZoN/linketinder

- **Autor: Athus**

## Funcionalidades

- Lista candidatos pre-cadastrados.
- Lista empresas pre-cadastradas.
- Mantem no minimo 5 candidatos em memoria.
- Mantem no minimo 5 empresas em memoria.
- Cadastra novos candidatos pelo terminal.
- Cadastra novas empresas pelo terminal.
- Armazena competencias de candidatos e empresas.
- Valida formato de CPF, CNPJ e CEP.
- Mantem vagas associadas as empresas.
- Permite que candidatos curtam vagas.
- Permite que empresas curtam candidatos.
- Identifica match quando candidato e empresa se curtem para a mesma vaga.

## Estrutura Principal

- `App.groovy`: ponto de entrada da aplicacao, controla o menu, os dados iniciais e os cadastros.
- `Pessoa.groovy`: classe base com atributos comuns, como nome, email, estado, CEP, descricao e competencias.
- `PessoaFisica.groovy`: representa um candidato, com CPF e idade.
- `PessoaJuridica.groovy`: representa uma empresa, com CNPJ e pais.
- `Vaga.groovy`: representa uma vaga publicada por uma empresa, com titulo, descricao e competencias esperadas.
- `Curtida.groovy`: representa a relacao de curtida entre candidato, empresa e vaga, alem de indicar quando ocorre match.
- `IPessoa.groovy`: interface criada para praticar conceitos de POO.

## Atualização: Banco de dados PostgreSQL
foi desenhado o **DER** do linketinder, utilizando o **dbdiagram.io**, a imagem gerada pelo diagrama está em `docs/derLinketinder.png`
- link: https://dbdiagram.io/d/Linketinder-6abbe06e5869425612cd1b34
- Os codigos SQL para criar o banco de dados e as tabelas estão em `docs/derLinketinder.sql`
- Para armazenar as senhas utilizei o `pgcrypto`, que é uma extensão do PostgreSQL que acrescenta funções de criptografia e hash, a principal função utilizada é a crypt():
```sql
crypt('Senha123', gen_salt('bf'))
```
> esse **gen_salt('bf')** gera um valor aleatório chamado salt e seleciona o algoritmo bcrypt, e o `crypt()` usa a senha e o salt para produzir o hash que será gravado. Tudo isso é executado dentro de `BEGIN` e `COMMIT`, que são clausulas **DTL**, ou grava tudo ou não grava nada!

## Atualizacao: Curtidas e Match

Nesta versao foi implementado o sistema de curtidas do Linketinder.

O candidato pode selecionar uma vaga e curtir essa oportunidade. A empresa pode selecionar uma de suas vagas e curtir um candidato. Quando os dois lados curtem a mesma relacao de candidato e vaga, o sistema identifica o match e exibe uma mensagem no terminal.

As curtidas sao armazenadas em memoria durante a execucao do programa.

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

No cadastro de candidato, a aplicacao cria o usuario, cria o candidato e relaciona suas competencias dentro da mesma transacao. No cadastro de empresa, cria o usuario e utiliza o ID retornado para criar a empresa.

Para cadastrar uma vaga, o usuario seleciona uma empresa ja cadastrada, informa titulo, descricao e local, depois adiciona as competencias. Caso a competencia nao exista, ela e criada antes de gerar o relacionamento com a vaga.

Foi utilizado `commit` para confirmar as operacoes e `rollback` para desfazer tudo quando ocorre algum erro durante o cadastro.

Os arquivos relacionados ao banco estao em:

- `sql/Linketinder.sql`: criacao das tabelas e relacionamentos.
- `sql/DadosIniciais.sql`: candidatos, empresas, competencias e vagas ficticias.
- `docs/derLinketinder.png`: imagem atualizada do diagrama.

O menu atual permite listar e cadastrar candidatos e empresas, mostrar competencias, visualizar os feeds anonimos e cadastrar vagas relacionadas a uma empresa existente.

## Como Executar

No terminal, dentro da pasta do projeto:

```bash
./gradlew run
```

## Tecnologias

- Groovy
- Gradle
- Scanner para entrada de dados pelo terminal
- ArrayList para armazenar candidatos, empresas e competencias

## Observacoes da versao inicial

Os dados sao armazenados apenas em memoria. Ao encerrar o programa, os cadastros feitos pelo terminal nao sao salvos em arquivo ou banco de dados.

Na versao atual, os fluxos integrados por JDBC sao persistidos no PostgreSQL. As curtidas e o match ainda pertencem ao fluxo antigo em memoria e serao integrados em uma proxima etapa.
