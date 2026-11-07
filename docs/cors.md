# CORS

O frontend e o backend normalmente usam origens diferentes:
http://localhost:5173 e http://localhost:8080. O navegador verifica CORS para
permitir que o frontend leia respostas da API.

## Configuração

O Router aplica CorsFilter nos contextos de candidatos, empresas, vagas,
login, logout, recrutadores, curtidas e matches. A origem padrão autorizada
é http://localhost:5173.

~~~bash
FRONTEND_ORIGIN=http://localhost:5174 ./gradlew run
# Inicia o backend autorizando exatamente a origem informada.
~~~

No frontend, VITE_API_URL define o endereço da API. FRONTEND_ORIGIN, no
backend, define qual origem do navegador pode ler suas respostas. São
configurações diferentes e exigem reinício dos respectivos servidores
quando alteradas. Protocolo, hostname e porta fazem parte da origem:
localhost e 127.0.0.1 não são a mesma origem.

## Preflight

Um POST com Content-Type: application/json ou uma chamada com Authorization
pode exigir uma consulta prévia OPTIONS. O navegador pergunta se o método
e os headers da chamada são permitidos:

~~~http
OPTIONS /api/recrutadores
Origin: http://localhost:5173
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type, authorization
~~~

O filtro aceita somente Content-Type e Authorization, sem distinguir caixa,
e verifica os métodos configurados para o contexto:

| Contexto | Métodos permitidos no preflight |
|---|---|
| /api/candidatos | GET, POST, DELETE |
| /api/empresas | GET, POST |
| /api/vagas | GET, POST |
| /api/auth/login e /api/auth/logout | POST |
| /api/recrutadores | GET, POST, DELETE |
| /api/curtidas | POST |
| /api/matches | GET |

O preflight válido retorna 204, sem corpo, com Access-Control-Allow-Origin,
Access-Control-Allow-Methods e Access-Control-Allow-Headers. Este último
reflete os headers pedidos e permitidos; sem headers solicitados, usa
Content-Type. A permissão por contexto não garante que todo subcaminho aceite
o método: o handler também valida rota e método.

## Resposta efetiva

- A origem autorizada recebe Access-Control-Allow-Origin com seu valor exato.
- Vary: Origin é adicionado às respostas tratadas pelo filtro.
- Origem diferente recebe 403 antes do handler.
- Origem, método ou header incompatível no preflight recebe 403 sem corpo.
- Chamadas comuns sem Origin, como clientes de terminal, seguem para o handler.
- Os headers definidos pelo filtro permanecem nas respostas dos handlers,
  inclusive nos erros de autenticação/autorização.

CORS não autentica o usuário nem impede acesso por clientes fora do navegador.
O login emite token opaco; as operações protegidas exigem Bearer e verificam
sessão, papel e vínculo no backend. Um preflight aceito não concede permissão
para cadastrar vaga ou gerenciar recrutadores: isso depende do usuário.

Se aparecer erro de CORS, confira a origem exata, a rota registrada, o método
e os headers solicitados no OPTIONS, além do status e dos logs do backend.
Não use wildcard ou remova autenticação para contornar o problema.

Contratos HTTP: [api.md](api.md). Fluxo completo:
[integração frontend/backend](integracao-frontend-backend.md).
