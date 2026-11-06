# CORS

O Router aplica CorsFilter nos contextos de candidatos, empresas e vagas. A origem padrão é `http://localhost:5173`.

```bash
FRONTEND_ORIGIN=http://localhost:5174 ./gradlew run
# Inicia a API autorizando essa origem do frontend.
```

- Origem autorizada recebe Access-Control-Allow-Origin e Vary: Origin.
- Origem diferente recebe 403 antes do handler.
- Chamadas sem Origin, como curl, seguem para o handler.
- Preflight OPTIONS válido retorna 204 sem corpo.
- Preflight autoriza GET, POST e Content-Type.
- Origem, método ou header incompatível no preflight resulta em 403.
- Respostas dos handlers mantêm os headers definidos pelo filtro.

CORS não autentica usuários nem impede chamadas de clientes fora do navegador. Não há login ou sessão implementados.
