# CORS

O Router aplica CorsFilter nos contextos de candidatos, empresas, vagas e login. A origem padrão é `http://localhost:5173`.

```bash
FRONTEND_ORIGIN=http://localhost:5174 ./gradlew run
# Inicia a API autorizando essa origem do frontend.
```

- Origem autorizada recebe Access-Control-Allow-Origin e Vary: Origin.
- Origem diferente recebe 403 antes do handler.
- Chamadas sem Origin, como curl, seguem para o handler.
- Preflight OPTIONS válido retorna 204 sem corpo; login aceita somente POST.
- Preflight autoriza GET e POST em candidatos, empresas e vagas; DELETE é liberado apenas para candidatos; login aceita somente POST. O único header permitido é Content-Type.
- Origem, método ou header incompatível no preflight resulta em 403.
- Respostas dos handlers mantêm os headers definidos pelo filtro.

CORS não autentica usuários nem impede chamadas de clientes fora do navegador. O endpoint de login verifica credenciais, mas ainda não cria sessão nem autoriza chamadas subsequentes.
