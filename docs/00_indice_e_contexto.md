# 00 - Indice e contexto

Este conjunto de documentos explica a adaptacao feita na API depois da POC inicial de tarefas. A ideia e registrar o fluxo atual de banco, autenticacao, roles e tutores para que voce consiga consultar sem depender do historico da conversa.

## De onde a API saiu

A POC original tinha dois conceitos principais:

- `tarefas`: tabela e endpoints para testar uma API simples.
- `usuarios`: tabela criada depois para relacionar uma tarefa a um usuario autenticado.

Esse modelo serviu para validar Spring Boot, RDS, ECS, ALB, Flyway e comunicacao com o app iOS. Depois, ele deixou de ser suficiente porque o produto passou a seguir uma modelagem propria com:

- `providers`
- `users`
- `admins`
- `tutors`

A partir desse ponto, `tarefas` e `usuarios` viraram legado. Eles nao fazem mais parte do fluxo funcional atual.

## O que existe agora

A API atual esta organizada em tres pacotes principais:

- `Auth`: login social Apple/Google, validacao de token externo, emissao do JWT interno da API e validacao desse JWT.
- `User`: entidades JPA que representam as tabelas de identidade e perfil base: `Providers`, `Users`, `Admins`, `Tutors` e `RoleType`.
- `Tutors`: endpoints e regras de negocio para criar, atualizar e consultar o tutor do usuario autenticado.

O banco atual e criado por migrations Flyway:

- `V1__create_tarefas.sql`: legado da primeira POC.
- `V2__create_usuarios_and_link_tarefas.sql`: legado que adicionou usuarios e ligou tarefas a usuarios.
- `V3__create_auth_users_admins_model.sql`: novo modelo base de autenticacao.
- `V4__create_tutors_and_remove_legacy.sql`: tabela de tutors e remocao de `tarefas` e `usuarios`.

O arquivo `application.yml` centraliza as variaveis usadas pela API:

- conexao com PostgreSQL;
- Flyway ligado;
- Hibernate em modo `validate`;
- porta do servidor;
- issuer, segredo e TTL do JWT interno;
- client IDs de Apple e Google.

## Como ler estes documentos

Leia na ordem:

1. `01_migrations_flyway_e_banco.md`
2. `02_modelo_de_dados_entidades_e_repositories.md`
3. `03_autenticacao_login_social.md`
4. `04_authservice_jwt_roles_e_security.md`
5. `05_fluxo_de_tutors.md`
6. `06_fluxos_de_requisicao_e_debug.md`

Essa ordem acompanha o fluxo real:

1. O banco nasce pelas migrations.
2. As entidades Java mapeiam esse banco.
3. O app faz login com Apple/Google.
4. A API cria/atualiza `providers` e `users`.
5. A API emite JWT interno.
6. O app usa esse JWT para chamar `/api/tutors/me`.
7. O tutor e criado ou atualizado no novo modelo.

## Estado atual da autenticacao

A autenticacao e a autorizacao de `/api/**` estao centralizadas no Spring Security. O Resource Server valida o JWT interno HS256 (assinatura, `exp`, issuer e `typ`) usando o `JwtDecoder` configurado em `SecurityConfig`, e o claim `role` e convertido em authority Spring (`ROLE_TUTOR`, `ROLE_ADMIN`).

Regras do filtro:

- `POST /auth/login`, `OPTIONS /**` e `/actuator/health`/`/actuator/info` sao publicos;
- `/api/tutors/**` exige `ROLE_TUTOR`;
- qualquer outro `/api/**` exige autenticacao.

Com isso, os controllers nao validam mais o token manualmente. O `TutorsController` apenas le o usuario autenticado via `Jwt.getSubject()`, entao qualquer novo endpoint `/api/**` ja nasce protegido pelo Spring Security. A classe `InternalTokenVerifier`, usada na solucao temporaria anterior, foi removida.

