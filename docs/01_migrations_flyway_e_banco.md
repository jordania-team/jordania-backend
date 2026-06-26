# 01 - Migrations Flyway e banco

As migrations ficam em `backend/api/src/main/resources/db/migration`. O Flyway executa esses arquivos automaticamente quando a aplicacao sobe. Como o projeto usa `spring.jpa.hibernate.ddl-auto=validate`, o Hibernate nao cria nem altera tabelas sozinho; ele apenas valida se as entidades Java batem com o schema criado pelo Flyway.

## V1 - `create_tarefas`

`V1__create_tarefas.sql` criou a primeira tabela da POC:

```sql
CREATE TABLE tarefas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    descricao TEXT,
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);
```

Essa tabela era o centro da POC antiga. Ela nao faz mais parte do fluxo atual, mas a migration permanece no historico porque migrations ja aplicadas nao devem ser reescritas.

## V2 - `usuarios` e ligacao com tarefas

`V2__create_usuarios_and_link_tarefas.sql` criou a tabela antiga `usuarios`:

```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    nome VARCHAR(120),
    email VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_provider_subject UNIQUE (provider, provider_subject)
);
```

Depois, a migration adicionou `usuario_id` em `tarefas` e criou a FK para `usuarios`.

Esse era um modelo intermediario:

- o usuario era identificado por provider social;
- tarefas podiam pertencer a um usuario;
- ainda nao existiam roles, providers separados, admins ou tutors.

## V3 - modelo base de autenticacao

`V3__create_auth_users_admins_model.sql` introduziu a modelagem nova.

Primeiro, criou o enum PostgreSQL:

```sql
CREATE TYPE role_type AS ENUM ('tutor', 'admin');
```

Esse enum representa os papeis aceitos pela API. No Java, ele e mapeado por `RoleType` com os mesmos valores: `tutor` e `admin`.

Antes de migrar dados, a migration valida uma regra importante:

```sql
provider_subject must be globally unique across providers
```

Ela verifica se o mesmo `provider_subject` aparece em `usuarios` com providers diferentes. Se aparecer, a migration falha. Isso existe porque a nova tabela `providers` usa `provider_subject` como chave primaria global.

Depois, cria as tabelas:

```sql
providers(
    provider_subject PK,
    provider_name NOT NULL
)
```

`providers` guarda a identidade externa do usuario no provedor social. Exemplos:

- provider_name: `apple`
- provider_subject: subject unico que veio do identity token Apple

```sql
users(
    id PK,
    role NOT NULL,
    email,
    provider_id FK,
    created_at,
    updated_at
)
```

`users` e o usuario interno da API. O `id` dessa tabela e o UUID usado como `sub` no JWT interno.

```sql
admins(
    id PK,
    user_id FK
)
```

`admins` prepara a modelagem para usuarios administrativos. Nesta etapa ainda nao existem endpoints de admin.

### Migracao dos dados antigos

A V3 tambem copia dados de `usuarios` para o modelo novo:

- `usuarios.provider_subject` vira `providers.provider_subject`;
- `lower(usuarios.provider)` vira `providers.provider_name`;
- `usuarios.id` vira `users.id`;
- `users.role` recebe sempre `tutor`;
- `usuarios.email` vira `users.email`;
- `criado_em` e `atualizado_em` viram `created_at` e `updated_at`.

Isso permitiu manter usuarios antigos enquanto a API passava a usar `users`.

## V4 - tutors e remocao do legado

`V4__create_tutors_and_remove_legacy.sql` criou a tabela `tutors`:

```sql
CREATE TABLE tutors (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT false,
    img_url VARCHAR,
    birthday TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    reports_counter INT NOT NULL DEFAULT 0
);
```

Ela tambem adiciona constraints:

- `uk_tutors_username`: impede dois tutores com o mesmo username.
- `uk_tutors_user_id`: garante um tutor por usuario.
- `ck_tutors_username`: exige username no formato `^[a-z0-9][a-z0-9._]{2,29}$`.
- `ck_tutors_birthday`: impede data de nascimento no futuro.
- `ck_tutors_reports_counter`: impede contador negativo.

No fim, a V4 remove o legado:

```sql
DROP TABLE IF EXISTS tarefas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
```

Depois da V4, o banco funcional da POC atual e:

- `providers`
- `users`
- `admins`
- `tutors`
- `flyway_schema_history`

## Como validar no banco

Queries uteis:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

```sql
SELECT to_regclass('public.tarefas') AS tarefas,
       to_regclass('public.usuarios') AS usuarios;
```

Depois da V4, `tarefas` e `usuarios` devem retornar `null`.

```sql
SELECT provider_subject, provider_name
FROM providers;
```

```sql
SELECT id, role, email, provider_id, created_at, updated_at
FROM users;
```

```sql
SELECT id, user_id, name, username, is_private, img_url, birthday, updated_at, reports_counter
FROM tutors;
```

