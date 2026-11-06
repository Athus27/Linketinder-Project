# Arquitetura e construção da API

O backend reutiliza controllers, modelos e DAOs antes acessados pelo console. A entrada agora é HTTP, sem framework web.

## Responsabilidades

- ApiApp: instancia dependências, registra rotas e inicia o servidor.
- HttpServerConfig: configura host, porta e executor com pool de threads.
- Router: registra contextos no servidor e associa filtros; não recebe requisições por conta própria.
- CorsFilter: verifica origem e encerra o preflight antes do handler.
- Handlers: verificam caminho e método, leem JSON, chamam controllers e escrevem a resposta.
- Controllers: delegam aos DAOs sem depender de HttpExchange.
- DAOs: consultam e persistem dados via JDBC, controlando transações de cadastro.
- HttpLogger: registra status e duração das respostas dos handlers; respostas encerradas no filtro não passam por ele.

```text
HttpServer seleciona o contexto registrado
    ↓
CorsFilter
    ↓
Handler → Controller → DAO → PostgreSQL
    ↓
Resposta HTTP + log
```

## Como foi construída sem framework

1. Criamos o HttpServer com endereço e porta.
2. Montamos as dependências manualmente em ApiApp.
3. Registramos caminhos com createContext e um HttpHandler.
4. No handle, usamos HttpExchange para ler método, caminho e corpo.
5. JsonSlurper converte JSON em Map; o controller chama a persistência.
6. O handler configura Content-Type, envia status e tamanho e escreve bytes UTF-8.
7. withStream fecha a saída ao terminar; server.start inicia o atendimento.

O servidor seleciona contextos por prefixo. Handlers validam o caminho: apenas candidatos possuem consulta por ID; empresas e vagas aceitam somente a rota-base.

## Limitações conhecidas

- Não há autenticação, autorização nem validação completa dos campos na fronteira HTTP.
- DAOs capturam algumas exceções e devolvem lista vazia, null ou false. Falha no banco pode parecer ausência de dados; duplicidade pode virar 500.
- Há atualização e remoção na persistência, mas não expostas por HTTP.
- Testes de conexão não substituem testes ponta a ponta de cadastro e transação.
