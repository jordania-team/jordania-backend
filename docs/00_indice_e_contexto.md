# 00 - Indice e contexto

Este conjunto de documentos descreve o estado atual da API depois da migracao da POC de tarefas para o modelo real de autenticacao, usuarios e tutores.

## Estado atual

A API usa Spring Boot, PostgreSQL, Flyway e Spring Security Resource Server. O app faz login social com Apple ou Google, a API valida o token social e emite uma sessao propria composta por:

- access token JWT interno;
- refresh token opaco com rotacao;
- identificacao interna baseada em `users.id`.

O modelo funcional atual e:

- `providers`: identidade social validada;
- `users`: usuario interno, email, role e provider;
- `admins`: relacao futura para usuarios administradores;
- `tutors`: perfil de tutor do usuario;
- `refresh_tokens`: sessoes renovaveis do usuario.

As tabelas antigas `tarefas` e `usuarios` foram mantidas apenas como historico de migracao inicial e removidas pela V4. Elas nao fazem parte do fluxo funcional atual.

## Principais endpoints

Autenticacao e sessao:

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /users/me`

Tutor:

- `GET /api/tutors/me`
- `PUT /api/tutors/me`
- `POST /api/tutors/me/profile-image`
- `DELETE /api/tutors/me/profile-image`

Desenvolvimento local:

- `POST /dev/login?userId=<uuid>` somente com `SPRING_PROFILES_ACTIVE=local`

## Migrations

As migrations Flyway atuais sao:

- `V1__create_tarefas.sql`: cria a tabela inicial da POC.
- `V2__create_usuarios_and_link_tarefas.sql`: cria `usuarios` e liga tarefas a usuarios.
- `V3__create_auth_users_admins_model.sql`: cria `role_type`, `providers`, `users` e `admins`, migrando dados de `usuarios`.
- `V4__create_tutors_and_remove_legacy.sql`: cria `tutors` e remove `tarefas`/`usuarios`.
- `V5__create_refresh_tokens.sql`: cria `refresh_tokens` vinculado a `users`.

## Ordem de leitura

1. `01_migrations_flyway_e_banco.md`
2. `02_modelo_de_dados_entidades_e_repositories.md`
3. `03_autenticacao_login_social.md`
4. `04_authservice_jwt_roles_e_security.md`
5. `05_fluxo_de_tutors.md`
6. `06_fluxos_de_requisicao_e_debug.md`
7. `07_refresh_tokens_e_sessao.md`
8. `08_imagens_s3.md`

## Regra central

O token social Apple/Google e usado somente no login. Depois disso, o app usa o JWT interno no header:

```http
Authorization: Bearer <token>
```

Quando esse access token expira, o app chama `/auth/refresh` com o refresh token atual e troca por um novo par `token + refreshToken`.
