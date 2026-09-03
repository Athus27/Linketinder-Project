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

## Atualizacao: Curtidas e Match

Nesta versao foi implementado o sistema de curtidas do Linketinder.

O candidato pode selecionar uma vaga e curtir essa oportunidade. A empresa pode selecionar uma de suas vagas e curtir um candidato. Quando os dois lados curtem a mesma relacao de candidato e vaga, o sistema identifica o match e exibe uma mensagem no terminal.

As curtidas sao armazenadas em memoria durante a execucao do programa.

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

## Observacoes

Os dados sao armazenados apenas em memoria. Ao encerrar o programa, os cadastros feitos pelo terminal nao sao salvos em arquivo ou banco de dados.
