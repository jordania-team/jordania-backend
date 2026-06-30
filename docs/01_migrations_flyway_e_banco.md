# 01 - Migrations Flyway e banco

O Hibernate roda com `ddl-auto: validate`. Por isso, toda alteracao de schema precisa estar em migration Flyway.

## V1 e V2 - legado da POC

`V1__create_tarefas.sql` criou a tabela `tarefas`.

`V2__create_usuarios_and_link_tarefas.sql` criou `usuarios` e adicionou `tarefas.usuario_id`.

Essas migrations existem para preservar a linha historica do banco. O modelo funcional atual nao usa mais essas tabelas.

## V3 - modelo base de autenticacao

`V3__create_auth_users_admins_model.sql` cria:

- enum PostgreSQL `role_type` com `tutor` e `admin`;
- tabela `providers`;
- tabela `users`;
- tabela `admins`;
- indices de apoio.

Ela tambem migra os dados existentes de `usuarios` para `providers` e `users`.

Regra importante: `users.id` vira o identificador interno usado no claim `sub` do JWT da API.

## V4 - tutors e remocao do legado

`V4__create_tutors_and_remove_legacy.sql` cria `tutors` com:

- um tutor por usuario (`uk_tutors_user_id`);
- username unico (`uk_tutors_username`);
- validacao de username;
- data de nascimento nao futura;
- contador de reports nao negativo.

No fim, a V4 remove:

```sql
DROP TABLE IF EXISTS tarefas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
```

Depois da V4, o banco funcional nao deve depender de `tarefas` nem de `usuarios`.

## V5 - refresh tokens

`V5__create_refresh_tokens.sql` cria a tabela de sessao renovavel:

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

Indices:

- `idx_refresh_tokens_family`: usado para revogar uma familia inteira quando ha reuse detection.
- `idx_refresh_tokens_user`: usado no logout para revogar os tokens ativos do usuario.

O banco nunca armazena o refresh token bruto. Apenas `token_hash`, calculado com SHA-256.

## Tabelas esperadas apos a V5

```text
providers
users
admins
tutors
refresh_tokens
flyway_schema_history
```

## Consultas uteis

Ver migrations aplicadas:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Ver usuarios e providers:

```sql
SELECT u.id, u.email, u.role, p.provider_name, p.provider_subject
FROM users u
JOIN providers p ON p.provider_subject = u.provider_id;
```

Ver refresh tokens ativos:

```sql
SELECT id, user_id, family_id, expires_at, revoked_at, created_at
FROM refresh_tokens
ORDER BY created_at DESC;
```
