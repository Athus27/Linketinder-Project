# Orientações do Linketinder

## Contexto técnico

- Backend Groovy na JVM/Java 21, Gradle Wrapper, `HttpServer` do JDK, JDBC e PostgreSQL com `pgcrypto`; testes Spock em `app/src/test` e `app/src/integrationTest`.
- O fluxo HTTP é `Router`/`CorsFilter` → handlers → controllers → DAOs → PostgreSQL. Os modelos ficam em `app/src/main/groovy/org/linketinder/model`.
- Comandos reais: `./gradlew run` inicia a API (padrão `http://localhost:8080`); `./gradlew check` executa testes unitários e de integração; `./gradlew check --rerun-tasks` repete as verificações.
- A configuração local fica em `app/src/main/resources/db.properties`, ignorada pelo Git. Nunca leia, exiba ou versione esse arquivo, `.env`, senhas ou credenciais. Use apenas configuração fictícia/`.env.example` quando existir.
- O schema inicial está em `sql/Linketinder.sql`, os dados de exemplo em `sql/DadosIniciais.sql` e as migrações em `sql/migrations`. O schema inicial só se aplica a banco vazio; migrações podem ser executadas em banco de desenvolvimento somente após conferir o alvo e realizar backup. É permitido implementar e testar em banco de testes, sem destruir dados existentes. Nunca publique segredos.

## Domínio observado

`User` é a base de `UserCandidate` e `UserCompany`; candidatos possuem competências, formação e vínculos com `Candidate`, empresas possuem vagas (`Vaga`) e competências são relacionadas por tabelas de vínculo. `Curtida` associa candidato e vaga com `curtida_candidato`, `curtida_empresa` e `data_match`. O match ocorre quando as duas curtidas do mesmo par candidato-vaga ficam verdadeiras. A API possui DAO/controller de curtida, mas o `Router` atual não expõe rotas HTTP para ela.

O login valida e-mail/senha e retorna ID, nome e tipo, mas não cria sessão/token e não protege os demais endpoints. O frontend seleciona menus em memória; isso não é autorização.

## Regra de anonimato e segurança

Antes do match, respostas HTTP e telas devem expor somente competências, formação e experiência permitidas pela regra de negócio, sem identidade de empresa, candidato ou recrutador. Depois do match, pode haver contato apropriado, nunca CPF, CNPJ, data de nascimento, senha ou hash. Não presumir que o código atual garante isso: GETs de candidatos/empresas e algumas telas ainda expõem identidade e dados identificáveis, e essa lacuna deve ser resolvida na especificação antes de implementar.

Valide autorização de ator e vínculo empresa-vaga-candidato no servidor; não confie em menus, IDs enviados pelo cliente ou `localStorage`. Scripts SQL/migrações são de estudo e podem ser destrutivos: revise o alvo, use dados fictícios e não execute exclusões ou mudanças de schema sem autorização explícita. Não faça PR, merge ou entrega pelo agente.
