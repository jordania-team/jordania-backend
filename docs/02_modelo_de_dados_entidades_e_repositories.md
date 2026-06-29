# 02 - Modelo de dados, entidades e repositories

O modelo atual separa identidade social, usuario interno, perfil de tutor e sessao renovavel.

## `providers`

Entidade: `Providers`

Representa a identidade social validada no login.

Campos principais:

- `provider_subject`: chave primaria, vinda do `sub` Apple/Google;
- `provider_name`: `apple` ou `google`.

Repository: `ProvidersRepository`.

## `users`

Entidade: `Users`

Representa o usuario interno da API. O `id` desta tabela e usado como `sub` do JWT interno.

Campos principais:

- `id`: UUID interno;
- `role`: enum `role_type`, hoje `tutor` ou `admin`;
- `email`;
- `provider`: referencia `providers.provider_subject`;
- `created_at`;
- `updated_at`.

Repository: `UsersRepository`.

Busca importante:

```java
findByProviderProviderSubject(String provider_subject)
```

## `tutors`

Entidade: `Tutors`

Representa o perfil de tutor do usuario.

Campos principais:

- `id`: UUID do tutor;
- `user`: referencia `users.id`;
- `name`;
- `username`;
- `is_private`;
- `img_url`;
- `birthday`;
- `updated_at`;
- `reports_counter`.

Repository: `TutorsRepository`.

Buscas importantes:

```java
findByUserId(UUID user_id)
findByUsername(String username)
```

## `admins`

Entidade: `Admins`

Hoje prepara o modelo para usuarios administradores. A autorizacao por role ja esta pronta no JWT e no `SecurityConfig`.

Repository: `AdminsRepository`.

## `refresh_tokens`

Entidade: `RefreshToken`

Representa um refresh token emitido para um usuario. O token bruto e retornado ao cliente somente uma vez; o banco guarda apenas o hash.

Campos principais:

- `id`: sequencial interno;
- `token_hash`: SHA-256 do refresh token bruto;
- `family_id`: identifica a familia de rotacao;
- `user`: referencia `users.id`;
- `expires_at`;
- `revoked_at`;
- `replaced_by`: hash do token que substituiu este token;
- `created_at`.

Repository: `RefreshTokenRepository`.

Operacoes importantes:

```java
findByTokenHash(String tokenHash)
revokeFamily(UUID familyId)
revokeAllForUser(UUID userId)
```

## Relacoes

```text
providers.provider_subject
    -> users.provider_id

users.id
    -> tutors.user_id
    -> admins.user_id
    -> refresh_tokens.user_id
```

## DTOs de sessao

`LoginResponse` retorna access token, dados basicos do usuario, role e refresh token:

```json
{
  "token": "jwt-interno",
  "userId": "uuid",
  "name": "Nome ou null",
  "email": "email@example.com",
  "role": "tutor",
  "refreshToken": "token-opaco",
  "refreshExpiresAt": "2026-07-26T12:00:00Z"
}
```

`UserResponse`, usado por `/users/me`, retorna:

```json
{
  "id": "uuid",
  "email": "email@example.com",
  "provider": "google",
  "role": "tutor",
  "name": "Nome do tutor ou null"
}
```
