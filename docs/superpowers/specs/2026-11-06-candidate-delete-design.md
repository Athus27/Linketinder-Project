# Exclusão de candidato pela API

## Objetivo

Permitir que a tela de gerenciamento exclua um candidato persistido no backend, mantendo a interface sincronizada com o resultado da API.

## Escopo

- Adicionar `DELETE /api/candidatos/{id}` ao handler existente.
- Liberar `DELETE` no CORS apenas para a rota de candidatos.
- Responder `204 No Content` ao excluir, `404 Not Found` quando o ID não existir, `400 Bad Request` para ID inválido e `500 Internal Server Error` em falha de persistência.
- No frontend, chamar a API antes de atualizar a lista; em erro, manter o candidato visível e informar a falha.
- Cobrir handler, CORS e service do frontend com testes.

## Fora de escopo

Implementar `PUT`/edição, exclusão de empresas ou vagas, autenticação e mudanças na modelagem do banco.

## Abordagem

O DAO e o controller já têm operação de remoção. A mudança reutilizará essa operação e acrescentará o roteamento HTTP/CORS, sem criar outra camada. O handler verificará a existência antes de remover. A operação de remoção retorna apenas `boolean`, então `false` após a consulta será tratado como falha de persistência (`500`). Limitação preexistente: `CandidateDAO.buscarPorId` também converte falhas de consulta em `null`; por isso uma falha de banco nessa verificação pode aparecer como `404`.

## Critério de aceite

Um candidato existente pode ser excluído pela tela e deixa de aparecer após resposta de sucesso; para falha ou ID inexistente, a interface preserva a lista e mostra uma mensagem apropriada. Testes e build dos dois projetos passam.
