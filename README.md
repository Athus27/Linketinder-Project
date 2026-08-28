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

## Estrutura Principal

- `App.groovy`: ponto de entrada da aplicacao, controla o menu, os dados iniciais e os cadastros.
- `Pessoa.groovy`: classe base com atributos comuns, como nome, email, estado, CEP, descricao e competencias.
- `PessoaFisica.groovy`: representa um candidato, com CPF e idade.
- `PessoaJuridica.groovy`: representa uma empresa, com CNPJ e pais.
- `IPessoa.groovy`: interface criada para praticar conceitos de POO.

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
